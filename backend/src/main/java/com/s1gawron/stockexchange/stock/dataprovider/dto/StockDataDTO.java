package com.s1gawron.stockexchange.stock.dataprovider.dto;

import com.s1gawron.stockexchange.stock.dataprovider.finnhub.dto.FinnhubCompanyProfileDTO;
import com.s1gawron.stockexchange.stock.dataprovider.finnhub.dto.FinnhubStockQuoteDTO;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record StockDataDTO(String ticker,
                           String companyFullName,
                           String companyOriginCountry,
                           String stockExchange,
                           String companyIndustry,
                           LocalDate ipoDate,
                           BigDecimal marketCapitalization,
                           double shareOutstanding,
                           StockQuoteDTO stockQuote,
                           Instant lastUpdateDate) {

    public static StockDataDTO createFrom(final FinnhubCompanyProfileDTO companyProfileResponse,
        final FinnhubStockQuoteDTO stockQuoteResponse) {
        final StockQuoteDTO stockQuoteDTO = StockQuoteDTO.createFrom(companyProfileResponse.currency(), stockQuoteResponse);
        final Instant lastUpdateDate = Instant.ofEpochSecond(stockQuoteResponse.lastUpdateDateInEpoch());

        return new StockDataDTO(companyProfileResponse.ticker(), companyProfileResponse.companyFullName(),
            companyProfileResponse.companyOriginCountry(), companyProfileResponse.stockExchange(), companyProfileResponse.companyIndustry(),
            companyProfileResponse.ipoDate(), companyProfileResponse.marketCapitalization(), companyProfileResponse.shareOutstanding(), stockQuoteDTO,
            lastUpdateDate);
    }

}
