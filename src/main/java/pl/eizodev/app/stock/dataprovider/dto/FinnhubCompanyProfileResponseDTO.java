package pl.eizodev.app.stock.dataprovider.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Builder
@Getter
@AllArgsConstructor
@JsonDeserialize(builder = FinnhubCompanyProfileResponseDTO.FinnhubCompanyProfileResponseDTOBuilder.class)
public class FinnhubCompanyProfileResponseDTO {

    @JsonProperty(value = "ticker")
    private final String ticker;

    @JsonProperty(value = "name")
    private final String companyFullName;

    @JsonProperty(value = "country")
    private final String companyOriginCountry;

    @JsonProperty(value = "exchange")
    private final String stockExchange;

    @JsonProperty(value = "finnhubIndustry")
    private final String companyIndustry;

    @JsonProperty(value = "ipo")
    private final String ipoDate;

    @JsonProperty(value = "marketCapitalization")
    private final BigDecimal marketCapitalization;

    @JsonProperty(value = "shareOutstanding")
    private final double shareOutstanding;

    @JsonProperty(value = "currency")
    private final String currency;

    @JsonPOJOBuilder(withPrefix = "")
    public static class FinnhubCompanyProfileResponseDTOBuilder {

    }

    public boolean isEmpty() {
        return this.ticker == null
            && this.companyFullName == null
            && this.companyOriginCountry == null
            && this.stockExchange == null
            && this.companyIndustry == null
            && this.ipoDate == null
            && this.marketCapitalization == null
            && this.shareOutstanding == 0
            && this.currency == null;
    }
}
