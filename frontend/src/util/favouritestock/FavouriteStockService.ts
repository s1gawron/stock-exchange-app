import authenticatedApiClient from "../AuthenticatedApiClient.ts";
import {UserFavouriteStockDTO} from "../../dto/stock/UserFavouriteStockDTO.ts";
import {addFavouriteStockUrl, removeFavouriteStockUrl, listFavouriteStocksUrl} from "./FavouriteStockServiceUrlProvider.ts";
import {errorResponse, ResponseDTO, successResponse} from "../../dto/ResponseDTO.ts";
import getErrMsg from "../ErrorMsgProvider.ts";

export async function addFavouriteStock(ticker: string): Promise<ResponseDTO<UserFavouriteStockDTO>> {
    try {
        const res = await authenticatedApiClient.post<UserFavouriteStockDTO>(addFavouriteStockUrl(), {ticker});
        return successResponse(res.data);
    } catch (err) {
        const errMsg = getErrMsg(err);
        return errorResponse(errMsg);
    }
}

export async function removeFavouriteStock(ticker: string): Promise<ResponseDTO<void>> {
    try {
        await authenticatedApiClient.delete(removeFavouriteStockUrl(ticker));
        return successResponse(undefined);
    } catch (err) {
        const errMsg = getErrMsg(err);
        return errorResponse(errMsg);
    }
}

export async function getUserFavouriteStocks(): Promise<ResponseDTO<UserFavouriteStockDTO[]>> {
    try {
        const res = await authenticatedApiClient.get<UserFavouriteStockDTO[]>(listFavouriteStocksUrl());
        return successResponse(res.data);
    } catch (err) {
        const errMsg = getErrMsg(err);
        return errorResponse(errMsg);
    }
}
