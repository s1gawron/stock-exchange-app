import {buildUrl} from "../../UrlProvider.ts";

export function walletUrl(): string {
    return buildUrl("api/user", "v2", "wallet");
}

export function userStocksUrl(): string {
    return buildUrl("api/user", "v2", "wallet/stocks");
}
