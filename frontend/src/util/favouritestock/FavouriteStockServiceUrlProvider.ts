import {buildUrl} from "../UrlProvider.ts";

export function addFavouriteStockUrl(): string {
    return buildUrl("api/user", "v1", "stock/favourites/add");
}

export function removeFavouriteStockUrl(ticker: string): string {
    return buildUrl("api/user", "v1", "stock/favourites/remove/" + ticker);
}

export function listFavouriteStocksUrl(): string {
    return buildUrl("api/user", "v1", "stock/favourites/list");
}
