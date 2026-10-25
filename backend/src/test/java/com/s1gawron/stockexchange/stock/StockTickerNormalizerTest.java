package com.s1gawron.stockexchange.stock;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class StockTickerNormalizerTest {

    @Test
    void shouldNormalizeTicker() {
        assertEquals("AAPL", StockTickerNormalizer.I.normalize(" aapl "));
        assertEquals("VOD.L", StockTickerNormalizer.I.normalize("vod.l"));
        assertEquals("", StockTickerNormalizer.I.normalize("   "));
    }

    @Test
    void shouldReturnNullWhenTickerIsNull() {
        assertNull(StockTickerNormalizer.I.normalize(null));
    }

}
