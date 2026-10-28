import authenticatedApiClient from "../AuthenticatedApiClient.ts";
import {UserFavouriteStockDTO} from "../../dto/stock/UserFavouriteStockDTO.ts";
import {addFavouriteStockUrl, removeFavouriteStockUrl, listFavouriteStocksUrl} from "./FavouriteStockServiceUrlProvider.ts";
import ResponseDTO from "../../dto/user/ResponseDTO.ts";
import getErrMsg from "../ErrorMsgProvider.ts";

export async function addFavouriteStock(ticker: string): Promise<ResponseDTO<UserFavouriteStockDTO | null>> {
    try {
        const res = await authenticatedApiClient.post(addFavouriteStockUrl(), {ticker});
        return new ResponseDTO(true, res.data);
    } catch (err) {
        const errMsg = getErrMsg(err);
        return new ResponseDTO(false, null, errMsg);
    }
}

export async function removeFavouriteStock(ticker: string): Promise<ResponseDTO<string | null>> {
    try {
        await authenticatedApiClient.delete(removeFavouriteStockUrl(ticker));
        return new ResponseDTO(true, "OK");
    } catch (err) {
        const errMsg = getErrMsg(err);
        return new ResponseDTO(false, null, errMsg);
    }
}

export async function getUserFavouriteStocks(): Promise<ResponseDTO<UserFavouriteStockDTO[] | null>> {
    try {
        const res = await authenticatedApiClient.get(listFavouriteStocksUrl());
        return new ResponseDTO(true, res.data);
    } catch (err) {
        const errMsg = getErrMsg(err);
        return new ResponseDTO(false, null, errMsg);
    }
}
