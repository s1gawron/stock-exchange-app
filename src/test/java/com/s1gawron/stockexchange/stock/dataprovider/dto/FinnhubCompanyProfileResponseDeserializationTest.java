package com.s1gawron.stockexchange.stock.dataprovider.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.s1gawron.stockexchange.shared.ObjectMapperCreator;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FinnhubCompanyProfileResponseDeserializationTest {

    private static final FinnhubCompanyProfileResponseDTO COMPANY_PROFILE_RESPONSE = new FinnhubCompanyProfileResponseDTO("AAPL", "Apple Inc", "US",
        "NASDAQ NMS - GLOBAL MARKET", "Technology", "1980-12-12", BigDecimal.valueOf(2458034), 16319.44, "USD", "https://finnhub.io/api/logo?symbol=AAPL",
        "14089961010.0", "https://www.apple.com/");

    private static final FinnhubCompanyProfileResponseDTO COMPANY_PROFILE_STOCK_NOT_FOUND_RESPONSE = new FinnhubCompanyProfileResponseDTO(null, null, null,
        null, null, null, null, 0, null, null, null, null);

    private final ObjectMapper mapper = ObjectMapperCreator.I.getMapper();

    @Test
    @SneakyThrows
    void shouldDeserialize() {
        final String companyProfileJsonResponse = Files.readString(Path.of("src/test/resources/finnhub-company-profile-response.json"));
        final FinnhubCompanyProfileResponseDTO result = mapper.readValue(companyProfileJsonResponse, FinnhubCompanyProfileResponseDTO.class);

        assertCompanyProfileResponse(COMPANY_PROFILE_RESPONSE, result);
    }

    @Test
    @SneakyThrows
    void shouldDeserializeWhenStockIsNotFound() {
        final String companyProfileJsonResponse = Files.readString(Path.of("src/test/resources/finnhub-company-profile-stock-not-found-response.json"));
        final FinnhubCompanyProfileResponseDTO result = mapper.readValue(companyProfileJsonResponse, FinnhubCompanyProfileResponseDTO.class);

        assertCompanyProfileResponse(COMPANY_PROFILE_STOCK_NOT_FOUND_RESPONSE, result);
    }

    private void assertCompanyProfileResponse(final FinnhubCompanyProfileResponseDTO expected, final FinnhubCompanyProfileResponseDTO result) {
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