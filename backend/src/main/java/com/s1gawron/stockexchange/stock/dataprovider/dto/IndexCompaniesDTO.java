package com.s1gawron.stockexchange.stock.dataprovider.dto;

import java.util.List;
import java.util.Map;

public record IndexCompaniesDTO(int count, Map<Character, List<IndexCompanyDTO>> indexCompanies) {

}
