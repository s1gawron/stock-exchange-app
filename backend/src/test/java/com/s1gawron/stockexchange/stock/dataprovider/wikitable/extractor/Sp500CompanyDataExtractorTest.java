package com.s1gawron.stockexchange.stock.dataprovider.wikitable.extractor;

import com.s1gawron.stockexchange.stock.dataprovider.dto.IndexCompaniesDTO;
import com.s1gawron.stockexchange.stock.dataprovider.dto.IndexCompanyDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class Sp500CompanyDataExtractorTest extends AbstractIndexCompanyDataExtractorTest {

    private Sp500CompanyDataExtractor underTest;

    @Override
    protected Path getFilePath() {
        return Path.of("src/test/resources/sp500-company-data-extractor-dataset.json");
    }

    @BeforeEach
    void setUp() throws IOException {
        underTest = new Sp500CompanyDataExtractor(getTableData());
    }

    @Test
    void shouldExtractData() {
        final IndexCompaniesDTO expected = new IndexCompaniesDTO(3, List.of(
            new IndexCompanyDTO("MMM", "3M", "Industrials"),
            new IndexCompanyDTO("AOS", "A. O. Smith", "Industrials"),
            new IndexCompanyDTO("ABT", "Abbott", "Health Care")
        ));

        final IndexCompaniesDTO result = underTest.extract();

        assertNotNull(result);
        assertEquals(expected, result);
    }
}