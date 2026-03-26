import axios from "axios";
import {stockDataUrl, findStockUrl} from "./StockServiceUrlProvider.ts";
import ResponseDTO from "../../dto/user/ResponseDTO.ts";
import StockDataDTO from "../../dto/stock/StockDataDTO.ts";
import {StockSearchDTO} from "../../dto/stock/StockSearchDTO.ts";
import getErrMsg from "../ErrorMsgProvider.ts";

export async function getStockData(ticker: string | undefined): Promise<ResponseDTO<StockDataDTO | null>> {
    if (ticker === undefined) {
        console.error("Ticker parameter is undefined!");
        return new ResponseDTO(false, null, "Stock ticker not provided!");
    }

    const stockDataUri = stockDataUrl(ticker);

    try {
        const res = await axios.get(stockDataUri);
        return new ResponseDTO(true, res.data)
    } catch (err) {
        const errMsg = getErrMsg(err);
        return new ResponseDTO(false, null, errMsg);
    }
}

export async function findStock(query: string): Promise<ResponseDTO<StockSearchDTO | null>> {
    const searchUri = findStockUrl(query);

    try {
        const res = await axios.get(searchUri);
        return new ResponseDTO(true, res.data);
    } catch (err) {
        const errMsg = getErrMsg(err);
        return new ResponseDTO(false, null, errMsg);
    }
}
