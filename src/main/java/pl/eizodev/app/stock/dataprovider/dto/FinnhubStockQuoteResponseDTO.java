package pl.eizodev.app.stock.dataprovider.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
@JsonDeserialize(builder = FinnhubStockQuoteResponseDTO.FinnhubStockQuoteResponseDTOBuilder.class)
public class FinnhubStockQuoteResponseDTO {

    @JsonProperty("c")
    private final BigDecimal currentPrice;

    @JsonProperty("d")
    private final BigDecimal priceChange;

    @JsonProperty("dp")
    private final BigDecimal percentageChange;

    @JsonProperty("h")
    private final BigDecimal highestPriceOfTheDay;

    @JsonProperty("l")
    private final BigDecimal lowestPriceOfTheDay;

    @JsonProperty("o")
    private final BigDecimal openPriceOfTheDay;

    @JsonProperty("pc")
    private final BigDecimal previousClosePrice;

    @JsonProperty("t")
    private final long lastUpdateDateInEpoch;

    @JsonPOJOBuilder(withPrefix = "")
    public static class FinnhubStockQuoteResponseDTOBuilder {

    }

    public boolean isEmpty() {
        return this.currentPrice.equals(new BigDecimal(0))
            && this.priceChange == null
            && this.percentageChange == null
            && this.highestPriceOfTheDay.equals(new BigDecimal(0))
            && this.lowestPriceOfTheDay.equals(new BigDecimal(0))
            && this.openPriceOfTheDay.equals(new BigDecimal(0))
            && this.previousClosePrice.equals(new BigDecimal(0))
            && this.lastUpdateDateInEpoch == 0;
    }
}
