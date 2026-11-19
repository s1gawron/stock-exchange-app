package com.stocksim.instrument;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.stocksim.marketdata.InstrumentListing;
import com.stocksim.marketdata.MarketDataProperties;
import com.stocksim.marketdata.MarketDataProvider;
import com.stocksim.marketdata.MarketDataUnavailableException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Loads the vendor symbol list into {@code instruments}: daily before the US open, and on startup when the last
 * import is missing, older than 20 hours, or came from a different provider.
 */
@Service
public class CatalogImportService {

	private static final Logger log = LoggerFactory.getLogger(CatalogImportService.class);

	private final MarketDataProvider provider;
	private final InstrumentRepository repository;
	private static final Duration STARTUP_REIMPORT_AFTER = Duration.ofHours(20);

	private final Set<String> importTypes;
	private final String providerName;
	private final JdbcClient jdbc;
	private final Clock clock;

	CatalogImportService(MarketDataProvider provider, InstrumentRepository repository,
			MarketDataProperties properties, JdbcClient jdbc, Clock clock) {
		this.provider = provider;
		this.repository = repository;
		this.importTypes = Set.copyOf(properties.importTypes());
		this.providerName = properties.provider();
		this.jdbc = jdbc;
		this.clock = clock;
	}

	@EventListener(ApplicationReadyEvent.class)
	void importOnStartupIfNeeded() {
		// The latest import of ANY provider decides: after a fake run, the catalog holds fake symbols.
		record LastImport(String provider, Instant at) {
		}
		Optional<LastImport> last = jdbc
			.sql("SELECT provider, imported_at FROM catalog_imports WHERE active_count > 0 ORDER BY id DESC LIMIT 1")
			.query((rs, n) -> new LastImport(rs.getString(1), rs.getTimestamp(2).toInstant()))
			.optional();
		boolean fresh = last.isPresent() && last.get().provider().equals(providerName)
				&& last.get().at().isAfter(clock.instant().minus(STARTUP_REIMPORT_AFTER));
		if (!fresh) {
			runImport();
		}
	}

	@Scheduled(cron = "${marketdata.catalog-import-cron:0 0 6 * * MON-FRI}", zone = "America/New_York")
	void scheduledImport() {
		runImport();
	}

	/** Returns the number of active instruments after the import, or -1 when the vendor was unavailable. */
	public int runImport() {
		try {
			List<InstrumentListing> listings = provider.listInstruments()
				.stream()
				.filter(l -> importTypes.contains(l.type()))
				.toList();
			if (listings.isEmpty()) {
				log.warn("Catalog import got no instruments; keeping the existing catalog");
				return (int) repository.countActive();
			}
			int active = repository.replaceCatalog(listings);
			jdbc.sql("INSERT INTO catalog_imports (provider, active_count) VALUES (?, ?)")
				.params(providerName, active)
				.update();
			log.info("Catalog import done: {} active instruments", active);
			return active;
		}
		catch (MarketDataUnavailableException ex) {
			log.warn("Catalog import skipped: {}", ex.getMessage());
			return -1;
		}
	}
}
