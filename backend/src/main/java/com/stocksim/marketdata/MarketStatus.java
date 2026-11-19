package com.stocksim.marketdata;

/** Whether the regular trading session is open now. {@code description} is a short human text. */
public record MarketStatus(boolean open, String description) {
}
