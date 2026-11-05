package com.s1gawron.stockexchange.transaction.matching.feed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Trade feed that receives no trades, so open transactions stay open. It will be replaced by the Finnhub websocket feed.
 */
@Component
public class NoOpTradeFeed implements TradeFeed {

    private static final Logger log = LoggerFactory.getLogger(NoOpTradeFeed.class);

    @Override
    public void subscribe(final String ticker) {
        log.debug("No trade feed configured, subscription to {} is ignored", ticker);
    }

    @Override
    public void unsubscribe(final String ticker) {
        log.debug("No trade feed configured, unsubscription from {} is ignored", ticker);
    }
}
