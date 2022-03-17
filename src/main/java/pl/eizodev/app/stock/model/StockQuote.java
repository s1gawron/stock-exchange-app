package pl.eizodev.app.stock.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.eizodev.app.stock.dataprovider.dto.FinnhubStockQuoteResponseDTO;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.TimeZone;

@Entity
@Table(name = "stock_quote")
@Getter
@NoArgsConstructor
public class StockQuote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private long id;

    @OneToOne(mappedBy = "stockQuote")
    private StockCompanyDetails stockCompanyDetails;

    @Column
    private String currency;

    @Column(name = "current_price")
    private BigDecimal currentPrice;

    @Column(name = "price_change")
    private BigDecimal priceChange;

    @Column(name = "percentage_change")
    private BigDecimal percentageChange;

    @Column(name = "highest_day")
    private BigDecimal highestPriceOfTheDay;

    @Column(name = "lowest_day")
    private BigDecimal lowestPriceOfTheDay;

    @JsonProperty("o")
    private BigDecimal openPriceOfTheDay;

    @Column(name = "last_close_price")
    private BigDecimal previousClosePrice;

    @Column(name = "last_update_date")
    private LocalDateTime lastUpdateDate;

    private StockQuote(final String currency, final BigDecimal currentPrice, final BigDecimal priceChange, final BigDecimal percentageChange,
        final BigDecimal highestPriceOfTheDay, final BigDecimal lowestPriceOfTheDay, final BigDecimal openPriceOfTheDay, final BigDecimal previousClosePrice,
        final LocalDateTime lastUpdateDate) {
        this.currency = currency;
        this.currentPrice = currentPrice;
        this.priceChange = priceChange;
        this.percentageChange = percentageChange;
        this.highestPriceOfTheDay = highestPriceOfTheDay;
        this.lowestPriceOfTheDay = lowestPriceOfTheDay;
        this.openPriceOfTheDay = openPriceOfTheDay;
        this.previousClosePrice = previousClosePrice;
        this.lastUpdateDate = lastUpdateDate;
    }

    public static StockQuote createFrom(final String currency, final FinnhubStockQuoteResponseDTO finnhubStockQuoteResponse) {
        final LocalDateTime lastUpdateDate = LocalDateTime.ofInstant(Instant.ofEpochSecond(finnhubStockQuoteResponse.getLastUpdateDate()),
            TimeZone.getDefault().toZoneId());

        return new StockQuote(currency, finnhubStockQuoteResponse.getCurrentPrice(), finnhubStockQuoteResponse.getPriceChange(),
            finnhubStockQuoteResponse.getPercentageChange(),
            finnhubStockQuoteResponse.getHighestPriceOfTheDay(), finnhubStockQuoteResponse.getLowestPriceOfTheDay(),
            finnhubStockQuoteResponse.getOpenPriceOfTheDay(), finnhubStockQuoteResponse.getPreviousClosePrice(), lastUpdateDate);
    }
}
