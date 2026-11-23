package com.stocksim.instrument;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import com.stocksim.marketdata.MarketDataProvider;
import com.stocksim.marketdata.MarketDataUnavailableException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class InstrumentService {

	private static final Logger log = LoggerFactory.getLogger(InstrumentService.class);

	private final InstrumentRepository repository;
	private final MarketDataProvider provider;

	InstrumentService(InstrumentRepository repository, MarketDataProvider provider) {
		this.repository = repository;
		this.provider = provider;
	}

	public List<InstrumentView> search(String query) {
		if (query == null || query.isBlank()) {
			return List.of();
		}
		return repository.search(query, 20).stream().map(InstrumentService::toView).toList();
	}

	/** Instrument with company details; the profile is fetched from the vendor once and stored. */
	public Optional<InstrumentView> get(String symbol) {
		String s = symbol.toUpperCase(Locale.ROOT);
		if (repository.find(s).isEmpty()) {
			return Optional.empty();
		}
		if (!repository.profileLoaded(s)) {
			try {
				provider.profile(s).ifPresentOrElse(p -> repository.saveProfile(s, p),
						() -> repository.markProfileMissing(s));
			}
			catch (MarketDataUnavailableException ex) {
				log.debug("Profile for {} not loaded: {}", s, ex.getMessage());
			}
		}
		return repository.find(s).map(InstrumentService::toView);
	}

	/** Plain database lookup (no vendor call). */
	public Optional<InstrumentSummary> find(String symbol) {
		return repository.find(symbol).map(r -> new InstrumentSummary(r.symbol(), r.name(), r.active()));
	}

	/** Display names for the given symbols; unknown symbols are missing from the map. */
	public Map<String, String> names(Collection<String> symbols) {
		return symbols.isEmpty() ? Map.of() : repository.names(symbols);
	}

	private static InstrumentView toView(InstrumentRow r) {
		return new InstrumentView(r.symbol(), r.displaySymbol(), r.name(), r.type(), r.currency(), r.exchange(),
				r.logoUrl(), r.webUrl(), r.industry(), r.active());
	}
}
