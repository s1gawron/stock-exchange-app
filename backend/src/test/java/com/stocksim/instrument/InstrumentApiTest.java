package com.stocksim.instrument;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.stocksim.IntegrationTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

/** The fake provider's catalog is imported at startup because the table is empty. */
class InstrumentApiTest extends IntegrationTest {

	@Autowired
	MockMvc mvc;

	@Autowired
	org.springframework.jdbc.core.simple.JdbcClient jdbc;

	@Test
	void startupImportIsRecordedPerProvider() {
		Integer active = jdbc.sql("SELECT active_count FROM catalog_imports WHERE provider = 'fake' ORDER BY id LIMIT 1")
			.query(Integer.class)
			.single();
		org.assertj.core.api.Assertions.assertThat(active).isEqualTo(8);
	}

	@Test
	void nameThatStartsWithQueryRanksBeforeNameThatOnlyContainsIt() {
		jdbc.sql("""
				INSERT INTO instruments (symbol, display_symbol, name, type) VALUES
				  ('PNAP', 'PNAP', 'Pineapple Holdings', 'Common Stock'),
				  ('APLX', 'APLX', 'Apple Test Corp', 'Common Stock'),
				  ('GRNA', 'GRNA', 'Green Apple Foods', 'Common Stock')
				ON CONFLICT (symbol) DO NOTHING
				""").update();
		var results = instruments.search("apple", 20);
		org.assertj.core.api.Assertions.assertThat(results).extracting(InstrumentRow::symbol)
			.containsSubsequence("APLX", "GRNA", "PNAP");
	}

	@Autowired
	InstrumentRepository instruments;

	@Autowired
	CatalogImportService catalogImport;

	@Test
	void startupReimportsWhenAnotherProviderImportedLast() {
		jdbc.sql("INSERT INTO catalog_imports (provider, active_count) VALUES ('finnhub', 27000)").update();
		int before = jdbc.sql("SELECT count(*) FROM catalog_imports").query(Integer.class).single();
		catalogImport.importOnStartupIfNeeded(); // current provider is fake; last import was finnhub
		int after = jdbc.sql("SELECT count(*) FROM catalog_imports").query(Integer.class).single();
		org.assertj.core.api.Assertions.assertThat(after).isEqualTo(before + 1);

		catalogImport.importOnStartupIfNeeded(); // now fake is latest and fresh: no import
		org.assertj.core.api.Assertions.assertThat(
				jdbc.sql("SELECT count(*) FROM catalog_imports").query(Integer.class).single())
			.isEqualTo(after);
	}

	@Test
	void searchRanksExactSymbolFirstThenMatchesNames() throws Exception {
		mvc.perform(get("/api/instruments").param("q", "meta").with(user("u")))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].symbol").value("META"));
		mvc.perform(get("/api/instruments").param("q", "micro").with(user("u")))
			.andExpect(jsonPath("$[0].symbol").value("MSFT"));
	}

	@Test
	void instrumentDetailsLoadProfileAndUnknownIs404() throws Exception {
		mvc.perform(get("/api/instruments/aapl").with(user("u")))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.exchange").value("NASDAQ (fake)"));
		mvc.perform(get("/api/instruments/NOPE").with(user("u"))).andExpect(status().isNotFound());
	}

	@Test
	void quoteAndMarketStatus() throws Exception {
		mvc.perform(get("/api/quotes/AAPL").with(user("u")))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.symbol").value("AAPL"))
			.andExpect(jsonPath("$.session").exists());
		mvc.perform(get("/api/market/status").with(user("u")))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.open").value(true));
	}
}
