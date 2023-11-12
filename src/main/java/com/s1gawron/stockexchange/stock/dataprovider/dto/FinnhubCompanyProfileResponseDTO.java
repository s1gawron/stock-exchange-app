package com.s1gawron.stockexchange.stock.dataprovider.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record FinnhubCompanyProfileResponseDTO(@JsonProperty(value = "ticker") String ticker, @JsonProperty(value = "name") String companyFullName,
                                               @JsonProperty(value = "country") String companyOriginCountry,
                                               @JsonProperty(value = "exchange") String stockExchange,
                                               @JsonProperty(value = "finnhubIndustry") String companyIndustry, @JsonProperty(value = "ipo") String ipoDate,
                                               @JsonProperty(value = "marketCapitalization") BigDecimal marketCapitalization,
                                               @JsonProperty(value = "shareOutstanding") double shareOutstanding,
                                               @JsonProperty(value = "currency") String currency, @JsonProperty(value = "logo") String companyLogoUrl,
                                               @JsonProperty(value = "phone") String companyPhone, @JsonProperty(value = "weburl") String companyWebsiteUrl) {

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
