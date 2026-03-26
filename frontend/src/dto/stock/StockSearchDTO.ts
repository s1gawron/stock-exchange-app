export interface StockSearchResultDTO {
    description: string;
    displaySymbol: string;
    symbol: string;
    type: string;
}

export interface StockSearchDTO {
    count: number;
    result: StockSearchResultDTO[];
}
