package com.s1gawron.stockexchange.stock.dataprovider.dto;

import java.util.List;

public record FinnhubStockSearchDTO(int count, List<FinnhubStockSearchDetailsDTO> result) {

}
