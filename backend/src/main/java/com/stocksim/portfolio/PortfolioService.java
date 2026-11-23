package com.stocksim.portfolio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.stocksim.account.AccountBalance;
import com.stocksim.account.AccountService;
import com.stocksim.instrument.InstrumentService;
import com.stocksim.instrument.InstrumentSummary;
import com.stocksim.ledger.LedgerService;
import com.stocksim.marketdata.MarketDataUnavailableException;
import com.stocksim.marketdata.Quote;
import com.stocksim.marketdata.QuoteService;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

@Service
public class PortfolioService {

	/** {@code reservedQty}: shares held by open sell orders. */
	public record Holding(String symbol, String name, BigDecimal qty, BigDecimal reservedQty, BigDecimal avgCost,
			BigDecimal costBasis, BigDecimal lastPrice, BigDecimal marketValue, BigDecimal unrealizedPnl,
			BigDecimal unrealizedPnlPercent) {
	}

	/**
	 * @param pricesIncomplete true when at least one holding has no current price; then market value, equity and
	 *                         return leave those holdings out.
	 */
	public record Portfolio(String currency, BigDecimal cash, BigDecimal reservedCash, BigDecimal availableCash,
			BigDecimal marketValue, BigDecimal equity,
			BigDecimal costBasis, BigDecimal unrealizedPnl, BigDecimal realizedPnl, BigDecimal deposits,
			BigDecimal totalReturn, BigDecimal totalReturnPercent, boolean pricesIncomplete, List<Holding> holdings) {
	}

	private final AccountService accounts;
	private final PositionRepository positions;
	private final InstrumentService instruments;
	private final QuoteService quotes;
	private final JdbcClient jdbc;
	private final LedgerService ledger;

	PortfolioService(AccountService accounts, PositionRepository positions, InstrumentService instruments,
			QuoteService quotes, JdbcClient jdbc, LedgerService ledger) {
		this.accounts = accounts;
		this.positions = positions;
		this.instruments = instruments;
		this.quotes = quotes;
		this.jdbc = jdbc;
		this.ledger = ledger;
	}

	public Portfolio get(long userId) {
		AccountBalance account = accounts.forUser(userId);
		List<Holding> holdings = new ArrayList<>();
		BigDecimal marketValue = BigDecimal.ZERO;
		BigDecimal costBasis = BigDecimal.ZERO;
		BigDecimal unrealized = BigDecimal.ZERO;
		boolean incomplete = false;

		for (Position p : positions.list(account.id())) {
			BigDecimal avgCost = p.costBasis().divide(p.qty(), 4, RoundingMode.HALF_UP);
			String name = instruments.find(p.symbol()).map(InstrumentSummary::name).orElse(p.symbol());
			costBasis = costBasis.add(p.costBasis());
			Optional<BigDecimal> price = price(p.symbol());
			if (price.isEmpty()) {
				incomplete = true;
				holdings.add(new Holding(p.symbol(), name, p.qty(), p.reservedQty(), avgCost, p.costBasis(), null, null,
						null, null));
				continue;
			}
			BigDecimal value = price.get().multiply(p.qty()).setScale(2, RoundingMode.HALF_UP);
			BigDecimal pnl = value.subtract(p.costBasis());
			marketValue = marketValue.add(value);
			unrealized = unrealized.add(pnl);
			holdings.add(new Holding(p.symbol(), name, p.qty(), p.reservedQty(), avgCost, p.costBasis(), price.get(),
					value, pnl, percent(pnl, p.costBasis())));
		}

		BigDecimal realized = jdbc.sql("""
				SELECT COALESCE(SUM(f.realized_pnl), 0) FROM fills f JOIN orders o ON o.id = f.order_id
				WHERE o.account_id = ?
				""").param(account.id()).query(BigDecimal.class).single();
		BigDecimal deposits = ledger.depositsOf(account.id());
		BigDecimal equity = account.cashBalance().add(marketValue);
		BigDecimal totalReturn = equity.subtract(deposits);
		return new Portfolio(account.currency(), account.cashBalance(), account.reservedCash(),
				account.availableCash(), marketValue, equity, costBasis, unrealized,
				realized, deposits, totalReturn, percent(totalReturn, deposits), incomplete, holdings);
	}

	private Optional<BigDecimal> price(String symbol) {
		try {
			return quotes.quote(symbol).map(Quote::price);
		}
		catch (MarketDataUnavailableException ex) {
			return Optional.empty();
		}
	}

	private static BigDecimal percent(BigDecimal part, BigDecimal whole) {
		return whole.signum() == 0 ? BigDecimal.ZERO
				: part.multiply(BigDecimal.valueOf(100)).divide(whole, 2, RoundingMode.HALF_UP);
	}
}
