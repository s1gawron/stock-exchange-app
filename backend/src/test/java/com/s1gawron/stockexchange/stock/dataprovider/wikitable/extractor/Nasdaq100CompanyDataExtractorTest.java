package com.s1gawron.stockexchange.stock.dataprovider.wikitable.extractor;

import com.s1gawron.stockexchange.stock.dataprovider.dto.IndexCompaniesDTO;
import com.s1gawron.stockexchange.stock.dataprovider.dto.IndexCompanyDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

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
    void shouldExtractData() {
        final IndexCompaniesDTO expected = new IndexCompaniesDTO(3, List.of(
            new IndexCompanyDTO("ADBE", "Adobe Inc.", "Information Technology"),
            new IndexCompanyDTO("ADP", "ADP", "Industrials"),
            new IndexCompanyDTO("ABNB", "Airbnb", "Consumer Discretionary")
        ));

        final IndexCompaniesDTO result = underTest.extract();

        assertNotNull(result);
        assertEquals(expected, result);
    }
}