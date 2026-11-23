package com.stocksim.instrument;

import java.time.Instant;
import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** Owns the {@code catalog_imports} table. */
@Repository
class CatalogImportRepository {

	/** Latest import with at least one active instrument. */
	record LastImport(String provider, Instant at) {
	}

	private final JdbcClient jdbc;

	CatalogImportRepository(JdbcClient jdbc) {
		this.jdbc = jdbc;
	}

	Optional<LastImport> lastWithActive() {
		return jdbc
			.sql("SELECT provider, imported_at FROM catalog_imports WHERE active_count > 0 ORDER BY id DESC LIMIT 1")
			.query((rs, n) -> new LastImport(rs.getString(1), rs.getTimestamp(2).toInstant()))
			.optional();
	}

	void record(String provider, int activeCount) {
		jdbc.sql("INSERT INTO catalog_imports (provider, active_count) VALUES (?, ?)")
			.params(provider, activeCount)
			.update();
	}
}
