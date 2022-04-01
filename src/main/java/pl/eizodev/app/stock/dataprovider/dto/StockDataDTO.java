package pl.eizodev.app.stock.dataprovider.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.TimeZone;

@AllArgsConstructor
@Getter
@Builder
@JsonDeserialize(builder = StockDataDTO.StockDataDTOBuilder.class)
public class StockDataDTO {

    private final String ticker;

    private final String companyFullName;

    private final String companyOriginCountry;

    private final String stockExchange;

    private final String companyIndustry;

    private final String ipoDate;

    private final BigDecimal marketCapitalization;

    private final double shareOutstanding;

    private final StockQuoteDTO stockQuote;

    private final String lastUpdateDate;

    public static StockDataDTO createFrom(final FinnhubCompanyProfileResponseDTO companyProfileResponse,
        final FinnhubStockQuoteResponseDTO stockQuoteResponse) {
        final StockQuoteDTO stockQuoteDTO = StockQuoteDTO.createFrom(companyProfileResponse.getCurrency(), stockQuoteResponse);
        final LocalDateTime lastUpdateDate = LocalDateTime.ofInstant(Instant.ofEpochSecond(stockQuoteResponse.getLastUpdateDateInEpoch()),
            TimeZone.getDefault().toZoneId());

        return new StockDataDTO(companyProfileResponse.getTicker(), companyProfileResponse.getCompanyFullName(),
            companyProfileResponse.getCompanyOriginCountry(), companyProfileResponse.getStockExchange(), companyProfileResponse.getCompanyIndustry(),
            companyProfileResponse.getIpoDate(), companyProfileResponse.getMarketCapitalization(), companyProfileResponse.getShareOutstanding(), stockQuoteDTO,
            lastUpdateDate.toString());
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class StockDataDTOBuilder {

    }
}
