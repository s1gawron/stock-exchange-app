package com.s1gawron.stockexchange.stock.dataprovider.dto;

import java.util.List;

public record FinnhubStockSearchResponseDTO(int count, List<FinnhubStockSearchDetailsDTO> result) {

}
