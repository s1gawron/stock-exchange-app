package com.s1gawron.stockexchange.stock.dataprovider.wikitable.extractor;

import com.s1gawron.stockexchange.stock.dataprovider.dto.IndexCompaniesDTO;
import com.s1gawron.stockexchange.stock.dataprovider.dto.IndexCompanyDTO;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public abstract class AbstractCompanyDataExtractor {

    public abstract IndexCompaniesDTO extract();

    protected Collector<IndexCompanyDTO, ?, Map<Character, List<IndexCompanyDTO>>> collectByTickerFirstLetter() {
        return Collectors.groupingBy(
            el -> el.ticker().charAt(0),
            TreeMap::new,
            Collectors.collectingAndThen(
                Collectors.toList(),
                list -> {
                    list.sort(Comparator.comparing(IndexCompanyDTO::ticker));
                    return list;
                }
            )
        );
    }

    protected int getAllElementsCount(final Collection<List<IndexCompanyDTO>> values) {
        return values.stream()
            .mapToInt(Collection::size)
            .sum();
    }

}
