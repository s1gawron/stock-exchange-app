package com.s1gawron.stockexchange.stock.dataprovider.wikitable.extractor;

import com.s1gawron.stockexchange.stock.dataprovider.dto.IndexCompaniesDTO;
import com.s1gawron.stockexchange.stock.dataprovider.dto.IndexCompanyDTO;

import java.util.List;

public class Sp500CompanyDataExtractor implements IndexCompanyDataExtractor {

    private final List<List<List<String>>> data;

    public Sp500CompanyDataExtractor(final List<List<List<String>>> data) {
        this.data = data;
    }

    @Override public IndexCompaniesDTO extract() {
        final List<IndexCompanyDTO> companies = data.get(0).stream()
            .skip(1) //skip data definition from array
            .map(o -> new IndexCompanyDTO(o.get(0), o.get(1), o.get(2)))
            .toList();

        return new IndexCompaniesDTO(companies.size(), companies);
    }
}
