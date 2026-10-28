import authenticatedApiClient from "../../AuthenticatedApiClient.ts";
import {UserWalletDTO} from "../../../dto/user/UserWalletDTO.ts";
import {userStocksUrl, walletUrl} from "./UserWalletServiceUrlProvider.ts";
import {errorResponse, ResponseDTO, successResponse} from "../../../dto/ResponseDTO.ts";
import getErrMsg from "../../ErrorMsgProvider.ts";
import {UserStockDTO} from "../../../dto/user/UserStockDTO.ts";

export async function getUserWalletDetails(): Promise<ResponseDTO<UserWalletDTO>> {
    const url = walletUrl();

    try {
        const res = await authenticatedApiClient.get<UserWalletDTO>(url);
        return successResponse(res.data);
    } catch (err) {
        const errMsg = getErrMsg(err);
        return errorResponse(errMsg);
    }
}

export async function getUserStocks(): Promise<ResponseDTO<UserStockDTO[]>> {
    const url = userStocksUrl();

    try {
        const res = await authenticatedApiClient.get<UserStockDTO[]>(url);
        return successResponse(res.data);
    } catch (err) {
        const errMsg = getErrMsg(err);
        return errorResponse(errMsg);
    }
}

