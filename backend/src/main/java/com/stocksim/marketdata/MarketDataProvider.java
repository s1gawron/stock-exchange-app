package com.stocksim.marketdata;

import java.util.List;
import java.util.Optional;

/** Single entry point to an external market data vendor. Swap implementations via {@code marketdata.provider}. */
public interface MarketDataProvider {

	/** Latest quote, or empty when the vendor does not know the symbol. */
	Optional<Quote> quote(String symbol);

	/** All listed instruments the vendor offers for our exchange (large list, call rarely). */
	List<InstrumentListing> listInstruments();

	/** Company details, or empty when not available. */
	Optional<CompanyProfile> profile(String symbol);

	MarketStatus marketStatus();
}
