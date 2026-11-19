package com.stocksim.trading;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

import com.stocksim.account.Account;
import com.stocksim.account.AccountRepository;
import com.stocksim.common.ApiException;
import com.stocksim.ledger.LedgerEntryType;
import com.stocksim.ledger.LedgerRepository;
import com.stocksim.marketdata.Quote;
import com.stocksim.portfolio.Position;
import com.stocksim.portfolio.PositionRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * The only place that changes cash and positions for trades. One transaction: lock account, check idempotency,
 * check funds or shares, then write order, fill, ledger entries and position together.
 */
@Component
class OrderExecutor {

	record Request(long accountId, String symbol, OrderSide side, BigDecimal qty, String idempotencyKey) {
	}

	private final AccountRepository accounts;
	private final OrderRepository orders;
	private final PositionRepository positions;
	private final LedgerRepository ledger;

	OrderExecutor(AccountRepository accounts, OrderRepository orders, PositionRepository positions,
			LedgerRepository ledger) {
		this.accounts = accounts;
		this.orders = orders;
		this.positions = positions;
		this.ledger = ledger;
	}

	/** Records an order that was rejected before execution (market closed, no quote, ...). */
	@Transactional
	public OrderView reject(Request req, RejectReason reason) {
		accounts.lock(req.accountId());
		Optional<OrderView> existing = replay(req);
		if (existing.isPresent()) {
			return existing.get();
		}
		return orders.get(insert(req, "REJECTED", reason));
	}

	@Transactional
	public OrderView execute(Request req, Quote quote) {
		Account account = accounts.lock(req.accountId());
		Optional<OrderView> existing = replay(req);
		if (existing.isPresent()) {
			return existing.get();
		}
		BigDecimal gross = quote.price().multiply(req.qty()).setScale(2, RoundingMode.HALF_UP);
		Optional<Position> position = positions.find(account.id(), req.symbol());
		BigDecimal heldQty = position.map(Position::qty).orElse(BigDecimal.ZERO);
		BigDecimal costBasis = position.map(Position::costBasis).orElse(BigDecimal.ZERO);

		if (req.side() == OrderSide.BUY) {
			if (account.cashBalance().compareTo(gross) < 0) {
				return orders.get(insert(req, "REJECTED", RejectReason.INSUFFICIENT_FUNDS));
			}
			long orderId = insert(req, "FILLED", null);
			orders.insertFill(orderId, req.qty(), quote.price(), gross, null, quote.timestamp());
			ledger.append(account.id(), LedgerEntryType.BUY, req.symbol(), gross.negate(), req.qty(), orderId);
			accounts.setCash(account.id(), account.cashBalance().subtract(gross));
			positions.save(account.id(), req.symbol(), heldQty.add(req.qty()), costBasis.add(gross));
			return orders.get(orderId);
		}

		if (heldQty.compareTo(req.qty()) < 0) {
			return orders.get(insert(req, "REJECTED", RejectReason.INSUFFICIENT_SHARES));
		}
		BigDecimal remainingQty = heldQty.subtract(req.qty());
		// Average-cost method: remove the sold share of the cost basis.
		BigDecimal removedBasis = remainingQty.signum() == 0 ? costBasis
				: costBasis.multiply(req.qty()).divide(heldQty, 2, RoundingMode.HALF_UP);
		BigDecimal realizedPnl = gross.subtract(removedBasis);
		long orderId = insert(req, "FILLED", null);
		orders.insertFill(orderId, req.qty(), quote.price(), gross, realizedPnl, quote.timestamp());
		ledger.append(account.id(), LedgerEntryType.SELL, req.symbol(), gross, req.qty().negate(), orderId);
		accounts.setCash(account.id(), account.cashBalance().add(gross));
		positions.save(account.id(), req.symbol(), remainingQty, costBasis.subtract(removedBasis));
		return orders.get(orderId);
	}

	/** Same key again: return the first result, unless the payload differs. */
	private Optional<OrderView> replay(Request req) {
		Optional<OrderView> existing = orders.findByIdempotencyKey(req.accountId(), req.idempotencyKey());
		existing.ifPresent(o -> {
			if (!o.symbol().equals(req.symbol()) || o.side() != req.side() || o.qty().compareTo(req.qty()) != 0) {
				throw new ApiException(HttpStatus.UNPROCESSABLE_CONTENT,
						"Idempotency-Key was already used for a different order");
			}
		});
		return existing;
	}

	private long insert(Request req, String status, RejectReason reason) {
		return orders.insert(req.accountId(), req.symbol(), req.side(), req.qty(), status, reason,
				req.idempotencyKey());
	}
}
