package com.stocksim.marketdata.fake;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.stocksim.marketdata.CompanyProfile;
import com.stocksim.marketdata.InstrumentListing;
import com.stocksim.marketdata.MarketDataProperties;
import com.stocksim.marketdata.MarketDataProvider;
import com.stocksim.marketdata.MarketStatus;
import com.stocksim.marketdata.Quote;

/**
 * Offline provider for tests and local development without a Finnhub key. Prices move slowly around a base value;
 * tests can pin prices and open/close the market.
 */
public class FakeMarketDataProvider implements MarketDataProvider {

	private static final Map<String, Object[]> BASE = new LinkedHashMap<>();

	static {
		BASE.put("AAPL", new Object[] { "Apple Inc", "Common Stock", 230.00 });
		BASE.put("MSFT", new Object[] { "Microsoft Corp", "Common Stock", 420.00 });
		BASE.put("NVDA", new Object[] { "NVIDIA Corp", "Common Stock", 140.00 });
		BASE.put("AMZN", new Object[] { "Amazon.com Inc", "Common Stock", 200.00 });
		BASE.put("GOOGL", new Object[] { "Alphabet Inc-Cl A", "Common Stock", 175.00 });
		BASE.put("TSLA", new Object[] { "Tesla Inc", "Common Stock", 250.00 });
		BASE.put("META", new Object[] { "Meta Platforms Inc-Class A", "Common Stock", 580.00 });
		BASE.put("SPY", new Object[] { "SPDR S&P 500 ETF Trust", "ETP", 590.00 });
	}

	private final Clock clock;
	private final Map<String, BigDecimal> pinnedPrices = new ConcurrentHashMap<>();
	private volatile boolean marketOpen;

	public FakeMarketDataProvider(Clock clock, MarketDataProperties.Fake config) {
		this.clock = clock;
		this.marketOpen = config.marketOpen();
	}

	public void setMarketOpen(boolean open) {
		this.marketOpen = open;
	}

	public void pinPrice(String symbol, BigDecimal price) {
		pinnedPrices.put(symbol, price);
	}

	public void reset() {
		pinnedPrices.clear();
		marketOpen = true;
	}

	@Override
	public Optional<Quote> quote(String symbol) {
		Object[] base = BASE.get(symbol.toUpperCase(Locale.ROOT));
		if (base == null) {
			return Optional.empty();
		}
		Instant now = clock.instant();
		double basePrice = (double) base[2];
		BigDecimal pinned = pinnedPrices.get(symbol);
		BigDecimal price = pinned != null ? pinned
				: BigDecimal.valueOf(basePrice * (1 + 0.01 * Math.sin(now.getEpochSecond() / 60.0 + symbol.hashCode())))
					.setScale(4, RoundingMode.HALF_UP);
		BigDecimal prevClose = BigDecimal.valueOf(basePrice).setScale(4, RoundingMode.HALF_UP);
		BigDecimal change = price.subtract(prevClose);
		BigDecimal changePct = change.multiply(BigDecimal.valueOf(100)).divide(prevClose, 2, RoundingMode.HALF_UP);
		return Optional.of(new Quote(symbol, price, change, changePct, price.max(prevClose), price.min(prevClose),
				prevClose, prevClose, now));
	}

	@Override
	public List<InstrumentListing> listInstruments() {
		return BASE.entrySet()
			.stream()
			.map(e -> new InstrumentListing(e.getKey(), e.getKey(), (String) e.getValue()[0],
					(String) e.getValue()[1], "USD", "XNAS", null))
			.toList();
	}

	@Override
	public Optional<CompanyProfile> profile(String symbol) {
		Object[] base = BASE.get(symbol);
		return base == null ? Optional.empty()
				: Optional.of(new CompanyProfile((String) base[0], "NASDAQ (fake)", "USD", null, null, "Fake data"));
	}

	@Override
	public MarketStatus marketStatus() {
		return marketOpen ? new MarketStatus(true, "Open (fake provider)")
				: new MarketStatus(false, "Closed (fake provider)");
	}
}
