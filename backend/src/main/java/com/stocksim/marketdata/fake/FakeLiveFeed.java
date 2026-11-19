package com.stocksim.marketdata.fake;

import java.util.Set;

import com.stocksim.marketdata.LiveFeed;
import com.stocksim.marketdata.PriceTick;
import com.stocksim.marketdata.TickStore;

import org.springframework.scheduling.annotation.Scheduled;

/** Emits a tick every few seconds for followed symbols, using the fake provider's prices. */
public class FakeLiveFeed implements LiveFeed {

	private final FakeMarketDataProvider provider;
	private final TickStore ticks;
	private volatile Set<String> symbols = Set.of();

	FakeLiveFeed(FakeMarketDataProvider provider, TickStore ticks) {
		this.provider = provider;
		this.ticks = ticks;
	}

	@Override
	public void setSymbols(Set<String> symbols) {
		this.symbols = Set.copyOf(symbols);
	}

	@Override
	public boolean isLive(String symbol) {
		return symbols.contains(symbol);
	}

	@Override
	public int maxSymbols() {
		return 50;
	}

	@Scheduled(fixedDelayString = "${marketdata.fake.tick-interval:PT2S}",
			initialDelayString = "${marketdata.fake.tick-interval:PT2S}")
	void emit() {
		for (String s : symbols) {
			provider.quote(s).ifPresent(q -> ticks.record(new PriceTick(s, q.price(), q.timestamp())));
		}
	}
}
