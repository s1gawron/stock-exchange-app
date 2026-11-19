package com.stocksim.realtime;

import java.util.LinkedHashSet;
import java.util.Set;

import com.stocksim.marketdata.LiveFeed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Decides which symbols the live feed follows, by priority, within the feed's symbol limit:
 * 1. open orders (needed for matching), 2. holdings, 3. watchlists, 4. symbols open in a browser.
 * Symbols that do not fit still work through REST quotes (slower, rate-limited).
 */
@Component
class SubscriptionManager {

	private static final Logger log = LoggerFactory.getLogger(SubscriptionManager.class);

	private final LiveFeed feed;
	private final PriceHub hub;
	private final JdbcClient jdbc;
	private int lastDropped = 0;

	SubscriptionManager(LiveFeed feed, PriceHub hub, JdbcClient jdbc) {
		this.feed = feed;
		this.hub = hub;
		this.jdbc = jdbc;
	}

	@Scheduled(fixedDelayString = "${marketdata.subscription-refresh:PT10S}", initialDelay = 3_000)
	void refresh() {
		Set<String> wanted = new LinkedHashSet<>();
		wanted.addAll(jdbc.sql("SELECT DISTINCT symbol FROM orders WHERE status = 'OPEN'").query(String.class).list());
		wanted.addAll(jdbc.sql("SELECT DISTINCT symbol FROM positions").query(String.class).list());
		wanted.addAll(jdbc.sql("""
				SELECT symbol FROM watchlist_items GROUP BY symbol ORDER BY count(*) DESC, symbol
				""").query(String.class).list());
		wanted.addAll(hub.viewedSymbols());

		Set<String> selected = new LinkedHashSet<>();
		for (String s : wanted) {
			if (selected.size() >= feed.maxSymbols()) {
				break;
			}
			selected.add(s);
		}
		int dropped = wanted.size() - selected.size();
		if (dropped > 0 && dropped != lastDropped) {
			log.warn("Live feed limit {} reached; {} symbols use REST quotes only", feed.maxSymbols(), dropped);
		}
		lastDropped = dropped;
		feed.setSymbols(selected);
	}
}
