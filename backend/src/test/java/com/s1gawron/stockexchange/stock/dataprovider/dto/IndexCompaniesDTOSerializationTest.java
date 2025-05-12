package com.s1gawron.stockexchange.stock.dataprovider.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.s1gawron.stockexchange.utils.ObjectMapperFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class IndexCompaniesDTOSerializationTest {

    private final ObjectMapper mapper = ObjectMapperFactory.I.getMapper();

    @Test
    void shouldSerialize() throws IOException {
        final IndexCompaniesDTO indexCompanies = new IndexCompaniesDTO(3, Map.ofEntries(
            Map.entry('A', List.of(
                new IndexCompanyDTO("AAPL", "Apple", "Information technology"),
                new IndexCompanyDTO("AMZN", "Amazon", "Retailing")
            )),
            Map.entry('M', List.of(
                new IndexCompanyDTO("MSFT", "Microsoft", "Information technology")
            ))
        ));

        final String indexCompaniesJsonResult = mapper.writeValueAsString(indexCompanies);
        final String expectedIndexCompaniesJsonResult = Files.readString(Path.of("src/test/resources/index-companies-dto.json"));

        final JsonNode expected = mapper.readTree(expectedIndexCompaniesJsonResult);
        final JsonNode result = mapper.readTree(indexCompaniesJsonResult);

        assertEquals(expected, result);
    }

}