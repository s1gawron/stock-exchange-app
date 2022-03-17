package pl.eizodev.app.stock.dataprovider.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@JsonDeserialize(builder = FinnhubStockSearchDetailsDTO.FinnhubStockSearchDetailsDTOBuilder.class)
public class FinnhubStockSearchDetailsDTO {

    private final String description;

    private final String displaySymbol;

    private final String symbol;

    private final String type;

    @JsonPOJOBuilder(withPrefix = "")
    public static class FinnhubStockSearchDetailsDTOBuilder {

    }
}
