import axios from "axios";
import {UserFavouriteStockDTO} from "../../dto/stock/UserFavouriteStockDTO.ts";
import {addFavouriteStockUrl, removeFavouriteStockUrl, listFavouriteStocksUrl} from "./FavouriteStockServiceUrlProvider.ts";
import ResponseDTO from "../../dto/user/ResponseDTO.ts";
import getErrMsg from "../ErrorMsgProvider.ts";
import {getToken} from "../AuthUtil.ts";

export async function addFavouriteStock(ticker: string): Promise<ResponseDTO<UserFavouriteStockDTO | null>> {
    const jwt = getToken();

    if (jwt === null) {
        return new ResponseDTO(false, null, "Unauthorized");
    }

    try {
        const res = await axios.post(addFavouriteStockUrl(), {ticker}, {
            headers: {Authorization: `Bearer ${jwt}`}
        });
        return new ResponseDTO(true, res.data);
    } catch (err) {
        const errMsg = getErrMsg(err);
        return new ResponseDTO(false, null, errMsg);
    }
}

export async function removeFavouriteStock(ticker: string): Promise<ResponseDTO<string | null>> {
    const jwt = getToken();

    if (jwt === null) {
        return new ResponseDTO(false, null, "Unauthorized");
    }

    try {
        await axios.delete(removeFavouriteStockUrl(ticker), {
            headers: {Authorization: `Bearer ${jwt}`}
        });
        return new ResponseDTO(true, "OK");
    } catch (err) {
        const errMsg = getErrMsg(err);
        return new ResponseDTO(false, null, errMsg);
    }
}

export async function getUserFavouriteStocks(): Promise<ResponseDTO<UserFavouriteStockDTO[] | null>> {
    const jwt = getToken();

    if (jwt === null) {
        return new ResponseDTO(false, null, "Unauthorized");
    }

    try {
        const res = await axios.get(listFavouriteStocksUrl(), {
            headers: {Authorization: `Bearer ${jwt}`}
        });
        return new ResponseDTO(true, res.data);
    } catch (err) {
        const errMsg = getErrMsg(err);
        return new ResponseDTO(false, null, errMsg);
    }
}
