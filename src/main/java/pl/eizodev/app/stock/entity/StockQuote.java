package pl.eizodev.app.stock.entity;

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

    @Column(name = "stock_ticker")
    private String ticker;

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

    @Column(name = "open_price_day")
    private BigDecimal openPriceOfTheDay;

    @Column(name = "last_close_price")
    private BigDecimal previousClosePrice;

    @Column(name = "last_update_date")
    private LocalDateTime lastUpdateDate;

    public StockQuote(final String ticker, final String currency, final BigDecimal currentPrice, final BigDecimal priceChange,
        final BigDecimal percentageChange,
        final BigDecimal highestPriceOfTheDay, final BigDecimal lowestPriceOfTheDay, final BigDecimal openPriceOfTheDay, final BigDecimal previousClosePrice,
        final LocalDateTime lastUpdateDate) {
        this.ticker = ticker;
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

    public static StockQuote createFrom(final String ticker, final String currency, final FinnhubStockQuoteResponseDTO finnhubStockQuoteResponse) {
        final LocalDateTime lastUpdateDate = LocalDateTime.ofInstant(Instant.ofEpochSecond(finnhubStockQuoteResponse.getLastUpdateDateInEpoch()),
            TimeZone.getDefault().toZoneId());

        return new StockQuote(ticker, currency, finnhubStockQuoteResponse.getCurrentPrice(), finnhubStockQuoteResponse.getPriceChange(),
            finnhubStockQuoteResponse.getPercentageChange(),
            finnhubStockQuoteResponse.getHighestPriceOfTheDay(), finnhubStockQuoteResponse.getLowestPriceOfTheDay(),
            finnhubStockQuoteResponse.getOpenPriceOfTheDay(), finnhubStockQuoteResponse.getPreviousClosePrice(), lastUpdateDate);
    }

    public void setCurrentPrice(final BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }

    public void setPriceChange(final BigDecimal priceChange) {
        this.priceChange = priceChange;
    }

    public void setPercentageChange(final BigDecimal percentageChange) {
        this.percentageChange = percentageChange;
    }

    public void setHighestPriceOfTheDay(final BigDecimal highestPriceOfTheDay) {
        this.highestPriceOfTheDay = highestPriceOfTheDay;
    }

    public void setLowestPriceOfTheDay(final BigDecimal lowestPriceOfTheDay) {
        this.lowestPriceOfTheDay = lowestPriceOfTheDay;
    }

    public void setOpenPriceOfTheDay(final BigDecimal openPriceOfTheDay) {
        this.openPriceOfTheDay = openPriceOfTheDay;
    }

    public void setPreviousClosePrice(final BigDecimal previousClosePrice) {
        this.previousClosePrice = previousClosePrice;
    }

    public void setLastUpdateDate(final LocalDateTime lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }
}
