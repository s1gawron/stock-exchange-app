package com.s1gawron.stockexchange.stock.dataprovider.finnhub.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.s1gawron.stockexchange.utils.ObjectMapperFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FinnhubCompanyProfileDTODeserializationTest {

    private static final FinnhubCompanyProfileDTO COMPANY_PROFILE_RESPONSE = new FinnhubCompanyProfileDTO("AAPL", "Apple Inc", "US",
        "NASDAQ NMS - GLOBAL MARKET", "Technology", LocalDate.of(1980, 12, 12), BigDecimal.valueOf(2458034), 16319.44, "USD",
        "https://finnhub.io/api/logo?symbol=AAPL", "14089961010.0", "https://www.apple.com/");

    private static final FinnhubCompanyProfileDTO COMPANY_PROFILE_STOCK_NOT_FOUND_RESPONSE = new FinnhubCompanyProfileDTO(null, null, null,
        null, null, null, null, 0, null, null, null, null);

    private final ObjectMapper mapper = ObjectMapperFactory.I.getMapper();

    @Test
    void shouldDeserialize() throws IOException {
        final String companyProfileJsonResponse = Files.readString(Path.of("src/test/resources/finnhub-company-profile-dto.json"));
        final FinnhubCompanyProfileDTO result = mapper.readValue(companyProfileJsonResponse, FinnhubCompanyProfileDTO.class);

        assertCompanyProfileResponse(COMPANY_PROFILE_RESPONSE, result);
    }

    @Test
    void shouldDeserializeWhenStockIsNotFound() throws IOException {
        final String companyProfileJsonResponse = Files.readString(Path.of("src/test/resources/finnhub-company-profile-stock-not-found-dto.json"));
        final FinnhubCompanyProfileDTO result = mapper.readValue(companyProfileJsonResponse, FinnhubCompanyProfileDTO.class);

        assertCompanyProfileResponse(COMPANY_PROFILE_STOCK_NOT_FOUND_RESPONSE, result);
    }

    private void assertCompanyProfileResponse(final FinnhubCompanyProfileDTO expected, final FinnhubCompanyProfileDTO result) {
        Assertions.assertAll(
            () -> assertEquals(expected.ticker(), result.ticker()),
            () -> assertEquals(expected.companyFullName(), result.companyFullName()),
            () -> assertEquals(expected.companyOriginCountry(), result.companyOriginCountry()),
            () -> assertEquals(expected.stockExchange(), result.stockExchange()),
            () -> assertEquals(expected.ipoDate(), result.ipoDate()),
            () -> assertEquals(expected.marketCapitalization(), result.marketCapitalization()),
            () -> assertEquals(expected.shareOutstanding(), result.shareOutstanding()),
            () -> assertEquals(expected.currency(), result.currency()),
            () -> assertEquals(expected.companyLogoUrl(), result.companyLogoUrl()),
            () -> assertEquals(expected.companyPhone(), result.companyPhone()),
            () -> assertEquals(expected.companyWebsiteUrl(), result.companyWebsiteUrl())
        );
    }

}