package pl.eizodev.app.stock.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.eizodev.app.stock.dataprovider.dto.FinnhubCompanyProfileResponseDTO;
import pl.eizodev.app.stock.dataprovider.dto.FinnhubStockQuoteResponseDTO;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "stock_company_details")
@Getter
@NoArgsConstructor
public class StockCompanyDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private long id;

    @Column(name = "stock_ticker")
    private String ticker;

    @Column(name = "company_full_name")
    private String companyFullName;

    @Column(name = "company_origin_country")
    private String companyOriginCountry;

    @Column(name = "stock_exchange")
    private String stockExchange;

    @Column(name = "company_industry")
    private String companyIndustry;

    @Column(name = "ipo_date")
    private String ipoDate;

    @Column(name = "market_capitalization")
    private BigDecimal marketCapitalization;

    @Column(name = "share_outstanding")
    private double shareOutstanding;

    @Column(name = "number_of_invokes")
    private long numberOfInvokes;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "stock_quote_id", referencedColumnName = "id")
    private StockQuote stockQuote;

    public StockCompanyDetails(final String ticker, final String companyFullName, final String companyOriginCountry, final String stockExchange,
        final String companyIndustry, final String ipoDate, final BigDecimal marketCapitalization, final double shareOutstanding, final long numberOfInvokes,
        final StockQuote stockQuote) {
        this.ticker = ticker;
        this.companyFullName = companyFullName;
        this.companyOriginCountry = companyOriginCountry;
        this.stockExchange = stockExchange;
        this.companyIndustry = companyIndustry;
        this.ipoDate = ipoDate;
        this.marketCapitalization = marketCapitalization;
        this.shareOutstanding = shareOutstanding;
        this.numberOfInvokes = numberOfInvokes;
        this.stockQuote = stockQuote;
    }

    public static StockCompanyDetails createFrom(final FinnhubCompanyProfileResponseDTO companyProfile, final FinnhubStockQuoteResponseDTO stockQuoteDTO) {
        final StockQuote stockQuote = StockQuote.createFrom(companyProfile.getTicker(), companyProfile.getCurrency(), stockQuoteDTO);

        return new StockCompanyDetails(companyProfile.getTicker(), companyProfile.getCompanyFullName(), companyProfile.getCompanyOriginCountry(),
            companyProfile.getStockExchange(), companyProfile.getCompanyIndustry(), companyProfile.getIpoDate(), companyProfile.getMarketCapitalization(),
            companyProfile.getShareOutstanding(), 0, stockQuote);
    }

    public void incrementNumberOfInvokes() {
        this.numberOfInvokes++;
    }

    public void setMarketCapitalization(final BigDecimal marketCapitalization) {
        this.marketCapitalization = marketCapitalization;
    }
}
