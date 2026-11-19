package com.stocksim.marketdata;

import java.util.Set;

/** Streaming trade feed. Implementations publish {@link PriceTick}s through {@link TickStore#record}. */
public interface LiveFeed {

	/** Replaces the set of followed symbols. Callers keep it within {@link #maxSymbols()}. */
	void setSymbols(Set<String> symbols);

	/** True when the feed is connected and follows this symbol, so its ticks are the current price. */
	boolean isLive(String symbol);

	int maxSymbols();
}
