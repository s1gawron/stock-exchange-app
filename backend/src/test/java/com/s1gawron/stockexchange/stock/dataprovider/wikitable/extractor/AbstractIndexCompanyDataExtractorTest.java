package com.s1gawron.stockexchange.stock.dataprovider.wikitable.extractor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.s1gawron.stockexchange.utils.ObjectMapperFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public abstract class AbstractIndexCompanyDataExtractorTest {

    private final ObjectMapper mapper = ObjectMapperFactory.I.getMapper();

    protected abstract Path getFilePath();

    protected List<List<List<String>>> getTableData() throws IOException {
        final String dataset = Files.readString(getFilePath());
        return mapper.readValue(dataset, List.class);
    }

}
