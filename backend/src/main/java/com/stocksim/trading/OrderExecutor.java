package com.stocksim.trading;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

import com.stocksim.account.AccountBalance;
import com.stocksim.account.AccountService;
import com.stocksim.common.ApiException;
import com.stocksim.ledger.LedgerEntryType;
import com.stocksim.ledger.LedgerService;
import com.stocksim.portfolio.PositionHolding;
import com.stocksim.portfolio.PositionService;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * The only place that changes cash, reservations and positions for orders. Every method is one transaction that
 * locks the account row first, then the order row (fixed order: no deadlocks).
 */
@Component
class OrderExecutor {

	record PlaceRequest(long accountId, String symbol, OrderSide side, OrderType type, BigDecimal qty,
			BigDecimal limitPrice, BigDecimal stopPrice, TimeInForce timeInForce, Instant expiresAt,
			String idempotencyKey) {
	}

	private final AccountService accounts;
	private final OrderRepository orders;
	private final PositionService positions;
	private final LedgerService ledger;
	private final FeeCalculator fees;
	private final BigDecimal stopBuyBuffer;

	OrderExecutor(AccountService accounts, OrderRepository orders, PositionService positions,
			LedgerService ledger, FeeCalculator fees, TradingProperties properties) {
		this.accounts = accounts;
		this.orders = orders;
		this.positions = positions;
		this.ledger = ledger;
		this.fees = fees;
		this.stopBuyBuffer = properties.stopBuyReserveBuffer();
	}

	/** Records an order rejected before execution (market closed, no quote, ...). */
	@Transactional
	public OrderView reject(PlaceRequest req, RejectReason reason) {
		accounts.lock(req.accountId());
		return replay(req).orElseGet(() -> orders.get(insert(req, OrderStatus.REJECTED, reason, null, null)));
	}

	/** Market order: fills now at the given price, or is rejected for lack of cash/shares. */
	@Transactional
	public OrderView executeMarket(PlaceRequest req, BigDecimal price, Instant quoteTime) {
		AccountBalance account = accounts.lock(req.accountId());
		Optional<OrderView> existing = replay(req);
		if (existing.isPresent()) {
			return existing.get();
		}
		long orderId = insert(req, OrderStatus.OPEN, null, null, null);
		fill(account, orderId, req.symbol(), req.side(), req.qty(), price, quoteTime);
		return orders.get(orderId);
	}

	/** Limit/stop order: reserves cash (BUY) or shares (SELL) and stays OPEN, or is rejected. */
	@Transactional
	public OrderView placePending(PlaceRequest req) {
		AccountBalance account = accounts.lock(req.accountId());
		Optional<OrderView> existing = replay(req);
		if (existing.isPresent()) {
			return existing.get();
		}
		if (req.side() == OrderSide.BUY) {
			BigDecimal reserve = buyReservation(req);
			if (account.availableCash().compareTo(reserve) < 0) {
				return orders.get(insert(req, OrderStatus.REJECTED, RejectReason.INSUFFICIENT_FUNDS, null, null));
			}
			accounts.updateBalances(account.id(), account.cashBalance(), account.reservedCash().add(reserve));
			return orders.get(insert(req, OrderStatus.OPEN, null, reserve, null));
		}
		BigDecimal available = positions.find(account.id(), req.symbol())
			.map(PositionHolding::availableQty)
			.orElse(BigDecimal.ZERO);
		if (available.compareTo(req.qty()) < 0) {
			return orders.get(insert(req, OrderStatus.REJECTED, RejectReason.INSUFFICIENT_SHARES, null, null));
		}
		positions.addReserved(account.id(), req.symbol(), req.qty());
		return orders.get(insert(req, OrderStatus.OPEN, null, null, req.qty()));
	}

	/** Fills a triggered OPEN order. Empty when the order is no longer OPEN (filled, cancelled, expired meanwhile). */
	@Transactional
	public Optional<OrderView> fillOpen(long orderId, BigDecimal price, Instant quoteTime) {
		Optional<Long> accountId = orders.accountIdOf(orderId);
		if (accountId.isEmpty()) {
			return Optional.empty();
		}
		AccountBalance account = accounts.lock(accountId.get());
		Optional<OrderRepository.OrderRow> row = orders.lockRow(orderId);
		if (row.isEmpty() || row.get().status() != OrderStatus.OPEN) {
			return Optional.empty();
		}
		OrderRepository.OrderRow o = row.get();
		account = release(account, o);
		fill(account, orderId, o.symbol(), o.side(), o.qty(), price, quoteTime);
		return Optional.of(orders.get(orderId));
	}

	@Transactional
	public OrderView cancel(long accountId, long orderId) {
		AccountBalance account = accounts.lock(accountId);
		OrderRepository.OrderRow o = orders.lockRow(orderId)
			.filter(r -> r.accountId() == accountId)
			.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order not found"));
		if (o.status() != OrderStatus.OPEN) {
			throw new ApiException(HttpStatus.CONFLICT, "Only open orders can be cancelled");
		}
		release(account, o);
		orders.close(orderId, OrderStatus.CANCELLED, null);
		return orders.get(orderId);
	}

	/** Expires a DAY order. Empty when it is no longer OPEN. */
	@Transactional
	public Optional<OrderView> expire(long orderId) {
		Optional<Long> accountId = orders.accountIdOf(orderId);
		if (accountId.isEmpty()) {
			return Optional.empty();
		}
		AccountBalance account = accounts.lock(accountId.get());
		Optional<OrderRepository.OrderRow> row = orders.lockRow(orderId);
		if (row.isEmpty() || row.get().status() != OrderStatus.OPEN) {
			return Optional.empty();
		}
		release(account, row.get());
		orders.close(orderId, OrderStatus.EXPIRED, null);
		return Optional.of(orders.get(orderId));
	}

	/** Gives back what the order reserved. Returns the account as it is after the release. */
	private AccountBalance release(AccountBalance account, OrderRepository.OrderRow o) {
		if (o.reservedQty().signum() > 0) {
			positions.addReserved(account.id(), o.symbol(), o.reservedQty().negate());
		}
		if (o.reservedCash().signum() == 0) {
			return account;
		}
		BigDecimal reserved = account.reservedCash().subtract(o.reservedCash());
		accounts.updateBalances(account.id(), account.cashBalance(), reserved);
		return new AccountBalance(account.id(), account.userId(), account.currency(), account.cashBalance(), reserved);
	}

	/** Writes fill, ledger, cash and position, and closes the order as FILLED, or REJECTED if it cannot be paid. */
	private void fill(AccountBalance account, long orderId, String symbol, OrderSide side, BigDecimal qty, BigDecimal price,
			Instant quoteTime) {
		BigDecimal gross = price.multiply(qty).setScale(2, RoundingMode.HALF_UP);
		BigDecimal fee = fees.fee(qty, gross);
		Optional<PositionHolding> position = positions.find(account.id(), symbol);
		BigDecimal heldQty = position.map(PositionHolding::qty).orElse(BigDecimal.ZERO);
		BigDecimal costBasis = position.map(PositionHolding::costBasis).orElse(BigDecimal.ZERO);
		BigDecimal reservedQty = position.map(PositionHolding::reservedQty).orElse(BigDecimal.ZERO);

		if (side == OrderSide.BUY) {
			BigDecimal total = gross.add(fee);
			if (account.availableCash().compareTo(total) < 0) {
				orders.close(orderId, OrderStatus.REJECTED, RejectReason.INSUFFICIENT_FUNDS);
				return;
			}
			orders.insertFill(orderId, qty, price, gross, fee, null, quoteTime);
			ledger.append(account.id(), LedgerEntryType.BUY, symbol, gross.negate(), qty, orderId);
			appendFee(account.id(), symbol, fee, orderId);
			accounts.updateBalances(account.id(), account.cashBalance().subtract(total), account.reservedCash());
			// Buy fee is part of the cost basis.
			positions.save(account.id(), symbol, heldQty.add(qty), costBasis.add(total), reservedQty);
			orders.close(orderId, OrderStatus.FILLED, null);
			return;
		}

		BigDecimal sellable = heldQty.subtract(reservedQty);
		if (sellable.compareTo(qty) < 0) {
			orders.close(orderId, OrderStatus.REJECTED, RejectReason.INSUFFICIENT_SHARES);
			return;
		}
		if (account.availableCash().add(gross).compareTo(fee) < 0) {
			orders.close(orderId, OrderStatus.REJECTED, RejectReason.INSUFFICIENT_FUNDS);
			return;
		}
		BigDecimal remainingQty = heldQty.subtract(qty);
		// Average-cost method: remove the sold share of the cost basis.
		BigDecimal removedBasis = remainingQty.signum() == 0 ? costBasis
				: costBasis.multiply(qty).divide(heldQty, 2, RoundingMode.HALF_UP);
		BigDecimal realizedPnl = gross.subtract(removedBasis).subtract(fee);
		orders.insertFill(orderId, qty, price, gross, fee, realizedPnl, quoteTime);
		ledger.append(account.id(), LedgerEntryType.SELL, symbol, gross, qty.negate(), orderId);
		appendFee(account.id(), symbol, fee, orderId);
		accounts.updateBalances(account.id(), account.cashBalance().add(gross).subtract(fee), account.reservedCash());
		positions.save(account.id(), symbol, remainingQty, costBasis.subtract(removedBasis), reservedQty);
		orders.close(orderId, OrderStatus.FILLED, null);
	}

	private void appendFee(long accountId, String symbol, BigDecimal fee, long orderId) {
		if (fee.signum() > 0) {
			ledger.append(accountId, LedgerEntryType.FEE, symbol, fee.negate(), BigDecimal.ZERO, orderId);
		}
	}

	/** Worst-case cost: limit price, or stop price plus buffer (stops fill at market after the trigger). */
	private BigDecimal buyReservation(PlaceRequest req) {
		BigDecimal unitPrice = req.type() == OrderType.LIMIT ? req.limitPrice()
				: req.stopPrice().multiply(BigDecimal.ONE.add(stopBuyBuffer));
		BigDecimal gross = unitPrice.multiply(req.qty()).setScale(2, RoundingMode.CEILING);
		return gross.add(fees.fee(req.qty(), gross));
	}

	/** Same key again: return the first result, unless the order details differ. */
	private Optional<OrderView> replay(PlaceRequest req) {
		Optional<OrderView> existing = orders.findByIdempotencyKey(req.accountId(), req.idempotencyKey());
		existing.ifPresent(o -> {
			boolean same = o.symbol().equals(req.symbol()) && o.side() == req.side() && o.type() == req.type()
					&& o.qty().compareTo(req.qty()) == 0 && samePrice(o.limitPrice(), req.limitPrice())
					&& samePrice(o.stopPrice(), req.stopPrice()) && o.timeInForce() == req.timeInForce();
			if (!same) {
				throw new ApiException(HttpStatus.UNPROCESSABLE_CONTENT,
						"Idempotency-Key was already used for a different order");
			}
		});
		return existing;
	}

	private static boolean samePrice(BigDecimal a, BigDecimal b) {
		return Objects.equals(a, b) || (a != null && b != null && a.compareTo(b) == 0);
	}

	private long insert(PlaceRequest req, OrderStatus status, RejectReason reason, BigDecimal reservedCash,
			BigDecimal reservedQty) {
		boolean open = status == OrderStatus.OPEN;
		return orders.insert(new OrderRepository.NewOrder(req.accountId(), req.symbol(), req.side(), req.type(),
				req.qty(), req.limitPrice(), req.stopPrice(), req.timeInForce(), status, reason,
				reservedCash == null ? BigDecimal.ZERO : reservedCash,
				reservedQty == null ? BigDecimal.ZERO : reservedQty, open ? req.expiresAt() : null,
				req.idempotencyKey()));
	}
}
