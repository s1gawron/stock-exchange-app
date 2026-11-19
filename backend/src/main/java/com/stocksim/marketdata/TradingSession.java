package com.stocksim.marketdata;

/** US equity session a moment falls into. Only REGULAR counts as "market open" for trading. */
public enum TradingSession {
	/** 04:00–09:30 New York. */
	PRE_MARKET,
	/** 09:30–16:00 (13:00 on early-close days). */
	REGULAR,
	/** Close until 4 hours later (16:00–20:00, or 13:00–17:00 on early-close days). */
	AFTER_HOURS,
	/** Overnight, weekends and holidays. */
	CLOSED
}
