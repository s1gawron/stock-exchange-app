package com.s1gawron.stockexchange.stock.dataprovider.wikitable.extractor;

import com.s1gawron.stockexchange.stock.dataprovider.dto.IndexCompaniesDTO;
import com.s1gawron.stockexchange.stock.dataprovider.dto.IndexCompanyDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DjiCompanyDataExtractorTest extends AbstractIndexCompanyDataExtractorTest {

    private DjiCompanyDataExtractor underTest;

    @Override
    protected Path getFilePath() {
        return Path.of("src/test/resources/dji-company-data-extractor-dataset.json");
    }

    @BeforeEach
    void setUp() throws IOException {
        underTest = new DjiCompanyDataExtractor(getTableData());
    }

    @Test
    void shouldExtractData() {
        final IndexCompaniesDTO expected = new IndexCompaniesDTO(3, List.of(
            new IndexCompanyDTO("MMM", "3M", "Conglomerate"),
            new IndexCompanyDTO("AXP", "American Express", "Financial services"),
            new IndexCompanyDTO("AMGN", "Amgen", "Biopharmaceutical")
        ));

        final IndexCompaniesDTO result = underTest.extract();

        assertNotNull(result);
        assertEquals(expected, result);
    }

}