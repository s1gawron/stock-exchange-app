import {buildUrl} from "../UrlProvider.ts";

export function createTransactionUrl(): string {
    return buildUrl("api/transaction", "v1", "create");
}
