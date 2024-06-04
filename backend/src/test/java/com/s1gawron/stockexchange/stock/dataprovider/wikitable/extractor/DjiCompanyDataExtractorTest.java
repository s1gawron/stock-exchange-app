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
    void shouldExtractDataWithAlphabeticalOrder() {
        final IndexCompaniesDTO expected = new IndexCompaniesDTO(3, Map.ofEntries(
            Map.entry('A', List.of(
                new IndexCompanyDTO("AMGN", "Amgen", "Biopharmaceutical"),
                new IndexCompanyDTO("AXP", "American Express", "Financial services")
            )),
            Map.entry('M', List.of(
                new IndexCompanyDTO("MMM", "3M", "Conglomerate")
            ))
        ));

        final IndexCompaniesDTO result = underTest.extract();

        assertNotNull(result);
        assertEquals(expected, result);
    }

}