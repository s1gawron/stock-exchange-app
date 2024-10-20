export interface UserStockDTO {
    ticker: string;
    name: string;
    price: number;
    priceChange: number;
    percentagePriceChange: number;
    quantity: number;
    quantityBlocked: number;
    averagePurchasePrice: number;
    profitLoss: number;
}
