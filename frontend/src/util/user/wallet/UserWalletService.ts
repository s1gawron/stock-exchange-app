import axios from "axios";
import {UserWalletDTO} from "../../../dto/user/UserWalletDTO";
import AuthUtil from "../../AuthUtil";
import UserWalletServiceUrlProvider from "./UserWalletServiceUrlProvider";
import ResponseDTO from "../../../dto/user/ResponseDTO";
import getErrMsg from "../../ErrorMsgProvider";
import {UserStockDTO} from "../../../dto/user/UserStockDTO";

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

