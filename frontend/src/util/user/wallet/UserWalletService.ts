import axios from "axios";
import {UserWalletDTO} from "../../../dto/user/UserWalletDTO.ts";
import AuthUtil from "../../AuthUtil.ts";
import UserWalletServiceUrlProvider from "./UserWalletServiceUrlProvider.ts";
import ResponseDTO from "../../../dto/user/ResponseDTO.ts";
import getErrMsg from "../../ErrorMsgProvider.ts";
import {UserStockDTO} from "../../../dto/user/UserStockDTO.ts";

export async function getUserWalletDetails(): Promise<ResponseDTO<UserWalletDTO | null>> {
    const url = UserWalletServiceUrlProvider.v2().wallet().provide();
    return await performUserWalletAction(url);
}

export async function getUserStocks(): Promise<ResponseDTO<UserStockDTO[] | null>> {
    const url = UserWalletServiceUrlProvider.v2().stocks().provide();
    return await performUserWalletAction(url);
}

async function performUserWalletAction(url: string): Promise<ResponseDTO<any>> {
    const jwt = AuthUtil.getToken();

    if (jwt === null) {
        return new ResponseDTO(false, null, "Unauthorized");
    }

    try {
        const res = await axios.get(url, {
            headers: {
                Authorization: `Bearer ${jwt}`
            }
        });
        return new ResponseDTO(true, res.data);
    } catch (err) {
        const errMsg = getErrMsg(err);
        return new ResponseDTO(false, null, errMsg);
    }
}

