import axios from "axios";
import {StockListingsDTO} from "../../dto/stock/StockListingsDTO.ts";
import StockServiceUrlProvider from "./StockServiceUrlProvider.ts";
import ResponseDTO from "../../dto/user/ResponseDTO.ts";
import StockDataDTO from "../../dto/stock/StockDataDTO.ts";
import getErrMsg from "../ErrorMsgProvider.ts";

export async function getIndexStockListings(index: string | undefined): Promise<ResponseDTO<StockListingsDTO | null>> {
    if (index === undefined) {
        console.error("Index parameter is undefined!");
        return new ResponseDTO(false, null, "Cannot load stock listings. Reason: index not provided!");
    }

    const indexStockListingsUri = StockServiceUrlProvider.v2().index(index).provide();

    try {
        const res = await axios.get(indexStockListingsUri);
        return new ResponseDTO(true, res.data)
    } catch (err) {
        const errMsg = getErrMsg(err);
        return new ResponseDTO(false, null, errMsg);
    }
}

export async function getStockData(ticker: string | undefined): Promise<ResponseDTO<StockDataDTO | null>> {
    if (ticker === undefined) {
        console.error("Ticker parameter is undefined!");
        return new ResponseDTO(false, null, "Stock ticker not provided!");
    }

    const stockDataUri = StockServiceUrlProvider.v2().stockData(ticker).provide();

    try {
        const res = await axios.get(stockDataUri);
        return new ResponseDTO(true, res.data)
    } catch (err) {
        const errMsg = getErrMsg(err);
        return new ResponseDTO(false, null, errMsg);
    }
}
