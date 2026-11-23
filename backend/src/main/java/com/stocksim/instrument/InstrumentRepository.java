package com.stocksim.instrument;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.stocksim.marketdata.CompanyProfile;
import com.stocksim.marketdata.InstrumentListing;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
class InstrumentRepository {

	private static final String COLUMNS = "symbol, display_symbol, name, type, currency, exchange, logo_url, web_url, industry, active";

	private final JdbcClient jdbc;
	private final JdbcTemplate jdbcTemplate;

	InstrumentRepository(JdbcClient jdbc, JdbcTemplate jdbcTemplate) {
		this.jdbc = jdbc;
		this.jdbcTemplate = jdbcTemplate;
	}

	Optional<InstrumentRow> find(String symbol) {
		return jdbc.sql("SELECT " + COLUMNS + " FROM instruments WHERE symbol = ?")
			.param(symbol)
			.query(InstrumentRepository::map)
			.optional();
	}

	boolean profileLoaded(String symbol) {
		return jdbc.sql("SELECT profile_loaded_at IS NOT NULL FROM instruments WHERE symbol = ?")
			.param(symbol)
			.query(Boolean.class)
			.optional()
			.orElse(false);
	}

	long countActive() {
		return jdbc.sql("SELECT count(*) FROM instruments WHERE active").query(Long.class).single();
	}

	/**
	 * Ranking: exact symbol, symbol prefix, name starts with query, a word in the name starts with query, then any
	 * name containing it ("apple" -> Apple Inc before Pineapple Co).
	 */
	List<InstrumentRow> search(String query, int limit) {
		String q = query.trim();
		String lower = escapeLike(q.toLowerCase());
		return jdbc.sql("""
				SELECT %s FROM instruments
				WHERE active AND (symbol LIKE :prefix OR lower(name) LIKE :contains)
				ORDER BY CASE WHEN symbol = :exact THEN 0
				              WHEN symbol LIKE :prefix THEN 1
				              WHEN lower(name) LIKE :namePrefix THEN 2
				              WHEN lower(name) LIKE :wordPrefix THEN 3
				              ELSE 4 END,
				         length(symbol), length(name), symbol
				LIMIT :limit
				""".formatted(COLUMNS))
			.param("exact", q.toUpperCase())
			.param("prefix", escapeLike(q.toUpperCase()) + "%")
			.param("namePrefix", lower + "%")
			.param("wordPrefix", "% " + lower + "%")
			.param("contains", "%" + lower + "%")
			.param("limit", limit)
			.query(InstrumentRepository::map)
			.list();
	}

	/** Upserts the listings, marks everything else inactive. Returns the number of active rows. */
	int replaceCatalog(List<InstrumentListing> listings) {
		Instant now = Instant.now();
		jdbcTemplate.batchUpdate("""
				INSERT INTO instruments (symbol, display_symbol, name, type, currency, mic, figi, active, updated_at)
				VALUES (?, ?, ?, ?, ?, ?, ?, TRUE, ?)
				ON CONFLICT (symbol) DO UPDATE SET display_symbol = EXCLUDED.display_symbol, name = EXCLUDED.name,
				  type = EXCLUDED.type, currency = EXCLUDED.currency, mic = EXCLUDED.mic, figi = EXCLUDED.figi,
				  active = TRUE, updated_at = EXCLUDED.updated_at
				""", listings, 1000, (ps, l) -> {
			ps.setString(1, l.symbol());
			ps.setString(2, truncate(l.displaySymbol(), 32));
			ps.setString(3, truncate(l.name(), 255));
			ps.setString(4, truncate(l.type(), 64));
			ps.setString(5, l.currency() != null && l.currency().length() == 3 ? l.currency() : null);
			ps.setString(6, truncate(l.mic(), 16));
			ps.setString(7, truncate(l.figi(), 32));
			ps.setTimestamp(8, Timestamp.from(now));
		});
		jdbc.sql("UPDATE instruments SET active = FALSE WHERE updated_at < ? AND active")
			.param(Timestamp.from(now))
			.update();
		return (int) countActive();
	}

	void saveProfile(String symbol, CompanyProfile p) {
		jdbc.sql("""
				UPDATE instruments SET exchange = :exchange, logo_url = :logo, web_url = :web, industry = :industry,
				  name = COALESCE(:name, name), profile_loaded_at = now()
				WHERE symbol = :symbol
				""")
			.param("exchange", truncate(p.exchange(), 128))
			.param("logo", truncate(p.logoUrl(), 512))
			.param("web", truncate(p.webUrl(), 512))
			.param("industry", truncate(p.industry(), 128))
			.param("name", truncate(p.name(), 255))
			.param("symbol", symbol)
			.update();
	}

	void markProfileMissing(String symbol) {
		jdbc.sql("UPDATE instruments SET profile_loaded_at = now() WHERE symbol = ?").param(symbol).update();
	}

	/** Display names for the given symbols (symbols without a row are missing from the map). */
	Map<String, String> names(Collection<String> symbols) {
		Map<String, String> result = new HashMap<>();
		jdbc.sql("SELECT symbol, name FROM instruments WHERE symbol = ANY (?)")
			.param(symbols.toArray(new String[0]))
			.query((rs, n) -> result.put(rs.getString(1), rs.getString(2)))
			.list();
		return result;
	}

	private static InstrumentRow map(ResultSet rs, int row) throws SQLException {
		return new InstrumentRow(rs.getString("symbol"), rs.getString("display_symbol"), rs.getString("name"),
				rs.getString("type"), rs.getString("currency"), rs.getString("exchange"), rs.getString("logo_url"),
				rs.getString("web_url"), rs.getString("industry"), rs.getBoolean("active"));
	}

	private static String escapeLike(String s) {
		return s.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
	}

	private static String truncate(String s, int max) {
		return s == null || s.length() <= max ? s : s.substring(0, max);
	}
}
