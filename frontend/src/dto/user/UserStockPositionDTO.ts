export interface UserStockPositionDTO {
    id: number;
    quantity: number;
    quantityBlocked: number;
    openPrice: number;
    openDate: string;
    profitLoss: number;
}
