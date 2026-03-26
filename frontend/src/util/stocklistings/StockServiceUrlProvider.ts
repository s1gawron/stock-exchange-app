import {buildUrl} from "../UrlProvider.ts";

export function stockDataUrl(ticker: string): string {
    return buildUrl("api/public/stock", "v2", ticker);
}

export function findStockUrl(query: string): string {
    return buildUrl("api/public/stock", "v2", "search?query=" + encodeURIComponent(query));
}
