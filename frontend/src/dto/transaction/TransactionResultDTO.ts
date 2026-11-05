import {TransactionType} from "./TransactionRequestDTO.ts";

export enum TransactionStatus {
    NEW = "NEW",
    PARTIALLY_FILLED = "PARTIALLY_FILLED",
    FILLED = "FILLED",
    CANCELED = "CANCELED"
}

export function isTransactionOpen(status: TransactionStatus): boolean {
    return status === TransactionStatus.NEW || status === TransactionStatus.PARTIALLY_FILLED;
}

export interface TransactionExecutionDTO {
    quantity: number;
    price: number;
    executionDate: string;
}

export interface TransactionResultDTO {
    transactionId: number;
    transactionType: TransactionType;
    transactionStatus: TransactionStatus;
    stockTicker: string;
    stockCurrency: string;
    stockPriceLimit: number;
    stockQuantity: number;
    filledQuantity: number;
    remainingQuantity: number;
    averageFillPrice: number | null;
    balanceBlocked: number;
    transactionCreateDate: string;
    transactionProcessDate: string | null;
    executions: TransactionExecutionDTO[];
}
