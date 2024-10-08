export enum TransactionType {
    PURCHASE = "PURCHASE",
    SELL = "SELL"
}

export interface TransactionRequestDTO {
    type: TransactionType;
    stockTicker: string;
    price: number;
    quantity: number;
}