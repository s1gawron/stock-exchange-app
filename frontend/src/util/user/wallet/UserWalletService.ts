import authenticatedApiClient from "../../AuthenticatedApiClient.ts";
import {UserWalletDTO} from "../../../dto/user/UserWalletDTO.ts";
import {userStocksUrl, walletUrl} from "./UserWalletServiceUrlProvider.ts";
import ResponseDTO from "../../../dto/ResponseDTO.ts";
import getErrMsg from "../../ErrorMsgProvider.ts";
import {UserStockDTO} from "../../../dto/user/UserStockDTO.ts";

export async function getUserWalletDetails(): Promise<ResponseDTO<UserWalletDTO | null>> {
    const url = walletUrl();

    try {
        const res = await authenticatedApiClient.get(url);
        return new ResponseDTO(true, res.data);
    } catch (err) {
        const errMsg = getErrMsg(err);
        return new ResponseDTO(false, null, errMsg);
    }
}

export async function getUserStocks(): Promise<ResponseDTO<UserStockDTO[] | null>> {
    const url = userStocksUrl();

    try {
        const res = await authenticatedApiClient.get(url);
        return new ResponseDTO(true, res.data);
    } catch (err) {
        const errMsg = getErrMsg(err);
        return new ResponseDTO(false, null, errMsg);
    }
}

