package com.s1gawron.stockexchange.stock.dataprovider.finnhub;

import com.s1gawron.stockexchange.stock.dataprovider.finnhub.dto.StockSearchDTO;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FinnhubStockDataProviderTest {

    private HttpServer finnhubServer;

    private String searchResponseBody;

    private FinnhubStockDataProvider finnhubStockDataProvider;

    @BeforeEach
    void setUp() throws IOException {
        finnhubServer = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        finnhubServer.createContext("/search", exchange -> {
            final byte[] body = searchResponseBody.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);

            try (final OutputStream responseBody = exchange.getResponseBody()) {
                responseBody.write(body);
            }
        });
        finnhubServer.start();

        final String baseUrl = "http://localhost:" + finnhubServer.getAddress().getPort();
        finnhubStockDataProvider = new FinnhubStockDataProvider("token", baseUrl);
    }

    @AfterEach
    void tearDown() {
        finnhubServer.stop(0);
    }

    @Test
    void shouldReturnSearchResults() {
        searchResponseBody = """
            {"count": 1, "result": [{"description": "APPLE INC", "displaySymbol": "AAPL", "symbol": "AAPL", "type": "Common Stock"}]}
            """;

        final StockSearchDTO result = finnhubStockDataProvider.findStock("apple");

        assertEquals(1, result.count());
        assertEquals("AAPL", result.result().get(0).symbol());
    }

    @Test
    void shouldReturnEmptyResultWhenNoStockMatchesQuery() {
        searchResponseBody = """
            {"count": 0, "result": []}
            """;

        final StockSearchDTO result = finnhubStockDataProvider.findStock("xyzxyz");

        assertEquals(0, result.count());
        assertTrue(result.result().isEmpty());
    }

    @Test
    void shouldReturnEmptyResultWhenFinnhubReturnsNoResultList() {
        searchResponseBody = """
            {"count": 0}
            """;

        final StockSearchDTO result = finnhubStockDataProvider.findStock("xyzxyz");

        assertEquals(0, result.count());
        assertTrue(result.result().isEmpty());
    }

}
