package com.s1gawron.stockexchange.stock.dataprovider.wikitable.extractor;

import com.s1gawron.stockexchange.stock.dataprovider.dto.IndexCompaniesDTO;
import com.s1gawron.stockexchange.stock.dataprovider.dto.IndexCompanyDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class Nasdaq100CompanyDataExtractorTest extends AbstractIndexCompanyDataExtractorTest {

    private Nasdaq100CompanyDataExtractor underTest;

    @Override
    protected Path getFilePath() {
        return Path.of("src/test/resources/nasdaq100-company-data-extractor-dataset.json");
    }

    @BeforeEach
    void setUp() throws IOException {
        underTest = new Nasdaq100CompanyDataExtractor(getTableData());
    }

    @Test
    void shouldExtractDataWithAlphabeticalOrder() {
        final IndexCompaniesDTO expected = new IndexCompaniesDTO(3, Map.ofEntries(
            Map.entry('A', List.of(
                new IndexCompanyDTO("ABNB", "Airbnb", "Consumer Discretionary"),
                new IndexCompanyDTO("ADBE", "Adobe Inc.", "Information Technology"),
                new IndexCompanyDTO("ADP", "ADP", "Industrials")
            ))
        ));

        final IndexCompaniesDTO result = underTest.extract();

        assertNotNull(result);
        assertEquals(expected, result);
    }
}