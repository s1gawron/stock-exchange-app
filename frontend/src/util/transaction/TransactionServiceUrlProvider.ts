import {buildUrl} from "../UrlProvider.ts";

export function createTransactionUrl(): string {
    return buildUrl("api/transaction", "v1", "create");
}

export function getTransactionsUrl(): string {
    // No trailing slash: the backend does not match "/api/transaction/v1/".
    return buildUrl("api", "transaction", "v1");
}

export function cancelTransactionUrl(transactionId: number): string {
    return buildUrl("api/transaction", "v1", `${transactionId}/cancel`);
}

export function modifyTransactionUrl(transactionId: number): string {
    return buildUrl("api/transaction", "v1", `${transactionId}`);
}
