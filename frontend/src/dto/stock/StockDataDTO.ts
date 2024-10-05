interface StockQuoteDTO {
    stockCurrency: string;
    currentPrice: number;
    priceChange: number;
    percentagePriceChange: number;
    highestPriceOfTheDay: number;
    lowestPriceOfTheDay: number;
    openPriceOfTheDay: number;
    previousClosePrice: number;
}

export default interface StockDataDTO {
    ticker: string;
    companyFullName: string;
    companyOriginCountry: string;
    stockExchange: string;
    companyIndustry: string;
    ipoDate: Date;
    marketCapitalization: number;
    shareOutstanding: number;
    stockQuote: StockQuoteDTO;
    lastUpdateDate: Date;
}