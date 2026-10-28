export interface FavouriteStockDataDTO {
    companyFullName: string;
    currentPrice: number;
    priceChange: number;
    percentagePriceChange: number;
    stockCurrency: string;
}

export interface FavouriteStockRowDTO {
    ticker: string;
    stockData?: FavouriteStockDataDTO;
}
