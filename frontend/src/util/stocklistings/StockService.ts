import axios from "axios";
import {stockDataUrl, findStockUrl} from "./StockServiceUrlProvider.ts";
import {errorResponse, ResponseDTO, successResponse} from "../../dto/ResponseDTO.ts";
import StockDataDTO from "../../dto/stock/StockDataDTO.ts";
import {StockSearchDTO} from "../../dto/stock/StockSearchDTO.ts";
import getErrMsg from "../ErrorMsgProvider.ts";

export async function getStockData(ticker: string | undefined): Promise<ResponseDTO<StockDataDTO>> {
    if (ticker === undefined) {
        console.error("Ticker parameter is undefined!");
        return errorResponse("Stock ticker not provided!");
    }

    const stockDataUri = stockDataUrl(ticker);

    try {
        const res = await axios.get<StockDataDTO>(stockDataUri);
        return successResponse(res.data);
    } catch (err) {
        const errMsg = getErrMsg(err);
        return errorResponse(errMsg);
    }
}

export async function findStock(query: string, signal?: AbortSignal): Promise<ResponseDTO<StockSearchDTO>> {
    const searchUri = findStockUrl(query);

    try {
        const res = await axios.get<StockSearchDTO>(searchUri, {signal});
        return successResponse(res.data);
    } catch (err) {
        const errMsg = getErrMsg(err);
        return errorResponse(errMsg);
    }
}
