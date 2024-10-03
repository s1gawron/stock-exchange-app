package com.s1gawron.stockexchange.stock.dataprovider.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.s1gawron.stockexchange.stock.dataprovider.finnhub.dto.FinnhubCompanyProfileDTO;
import com.s1gawron.stockexchange.stock.dataprovider.finnhub.dto.FinnhubStockQuoteDTO;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.TimeZone;

public record StockDataDTO(String ticker,
                           String companyFullName,
                           String companyOriginCountry,
                           String stockExchange,
                           String companyIndustry,
                           @JsonSerialize(using = LocalDateSerializer.class) @JsonDeserialize(using = LocalDateDeserializer.class) LocalDate ipoDate,
                           BigDecimal marketCapitalization,
                           double shareOutstanding,
                           StockQuoteDTO stockQuote,
                           @JsonSerialize(using = LocalDateTimeSerializer.class) @JsonDeserialize(using = LocalDateTimeDeserializer.class) LocalDateTime lastUpdateDate) {

    public static StockDataDTO createFrom(final FinnhubCompanyProfileDTO companyProfileResponse,
        final FinnhubStockQuoteDTO stockQuoteResponse) {
        final StockQuoteDTO stockQuoteDTO = StockQuoteDTO.createFrom(companyProfileResponse.currency(), stockQuoteResponse);
        final LocalDateTime lastUpdateDate = LocalDateTime.ofInstant(Instant.ofEpochSecond(stockQuoteResponse.lastUpdateDateInEpoch()),
            TimeZone.getDefault().toZoneId());

        return new StockDataDTO(companyProfileResponse.ticker(), companyProfileResponse.companyFullName(),
            companyProfileResponse.companyOriginCountry(), companyProfileResponse.stockExchange(), companyProfileResponse.companyIndustry(),
            companyProfileResponse.ipoDate(), companyProfileResponse.marketCapitalization(), companyProfileResponse.shareOutstanding(), stockQuoteDTO,
            lastUpdateDate);
    }

}
