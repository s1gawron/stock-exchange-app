package com.s1gawron.stockexchange.stock.dataprovider.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@JsonDeserialize(builder = FinnhubStockSearchResponseDTO.FinnhubStockSearchResponseDTOBuilder.class)
public class FinnhubStockSearchResponseDTO {

    private final int count;

    private final List<FinnhubStockSearchDetailsDTO> result;

    @JsonPOJOBuilder(withPrefix = "")
    public static class FinnhubStockSearchResponseDTOBuilder {

    }

}
