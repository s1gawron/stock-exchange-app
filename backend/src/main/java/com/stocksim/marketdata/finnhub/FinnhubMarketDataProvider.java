package com.stocksim.marketdata.finnhub;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import com.stocksim.marketdata.CompanyProfile;
import com.stocksim.marketdata.InstrumentListing;
import com.stocksim.marketdata.MarketDataProvider;
import com.stocksim.marketdata.MarketStatus;
import com.stocksim.marketdata.Quote;

class FinnhubMarketDataProvider implements MarketDataProvider {

	private final FinnhubClient client;
	private final FinnhubMarketStatus marketStatus;

	FinnhubMarketDataProvider(FinnhubClient client, FinnhubMarketStatus marketStatus) {
		this.client = client;
		this.marketStatus = marketStatus;
	}

	@Override
	public Optional<Quote> quote(String symbol) {
		FinnhubClient.QuoteResponse r = client.quote(symbol);
		// Finnhub answers unknown symbols with an all-zero body instead of 404.
		if (r == null || r.c() == null || r.c() <= 0 || r.t() == null || r.t() <= 0) {
			return Optional.empty();
		}
		return Optional.of(new Quote(symbol, price(r.c()), price(r.d()), scale2(r.dp()), price(r.h()), price(r.l()),
				price(r.o()), price(r.pc()), Instant.ofEpochSecond(r.t())));
	}

	@Override
	public List<InstrumentListing> listInstruments() {
		List<FinnhubClient.SymbolResponse> symbols = client.symbols("US");
		if (symbols == null) {
			return List.of();
		}
		return symbols.stream()
			.filter(s -> s.symbol() != null && !s.symbol().isBlank())
			.map(s -> new InstrumentListing(s.symbol(), orElse(s.displaySymbol(), s.symbol()),
					orElse(s.description(), s.symbol()), orElse(s.type(), "Unknown"), s.currency(), s.mic(),
					s.figi()))
			.toList();
	}

	@Override
	public Optional<CompanyProfile> profile(String symbol) {
		FinnhubClient.ProfileResponse r = client.profile(symbol);
		// Unknown symbol or no profile on our plan: Finnhub returns {}.
		if (r == null || r.ticker() == null) {
			return Optional.empty();
		}
		return Optional.of(new CompanyProfile(r.name(), r.exchange(), r.currency(), blankToNull(r.logo()),
				blankToNull(r.weburl()), blankToNull(r.finnhubIndustry())));
	}

	@Override
	public MarketStatus marketStatus() {
		return marketStatus.current();
	}

	private static BigDecimal price(Double v) {
		return BigDecimal.valueOf(v == null ? 0 : v).setScale(4, RoundingMode.HALF_UP);
	}

	private static BigDecimal scale2(Double v) {
		return BigDecimal.valueOf(v == null ? 0 : v).setScale(2, RoundingMode.HALF_UP);
	}

	private static String orElse(String v, String fallback) {
		return v == null || v.isBlank() ? fallback : v;
	}

	private static String blankToNull(String v) {
		return v == null || v.isBlank() ? null : v;
	}
}
