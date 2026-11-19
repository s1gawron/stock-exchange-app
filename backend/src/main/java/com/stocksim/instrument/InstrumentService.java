package com.stocksim.instrument;

import java.util.List;
import java.util.Locale;
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

	public List<Instrument> search(String query) {
		if (query == null || query.isBlank()) {
			return List.of();
		}
		return repository.search(query, 20);
	}

	/** Instrument with company details; the profile is fetched from the vendor once and stored. */
	public Optional<Instrument> get(String symbol) {
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
		return repository.find(s);
	}
}
