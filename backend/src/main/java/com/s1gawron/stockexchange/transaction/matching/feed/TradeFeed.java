package com.s1gawron.stockexchange.transaction.matching.feed;

/**
 * Source of real market trades. An implementation publishes the received trades to {@link TradeBuffer}.
 * <p>
 * Only tickers with open transactions are subscribed.
 */
public interface TradeFeed {

    void subscribe(String ticker);

    void unsubscribe(String ticker);

}
