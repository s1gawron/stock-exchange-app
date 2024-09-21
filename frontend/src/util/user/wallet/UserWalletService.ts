import axios from "axios";
import {UserWalletDTO} from "../../../dto/user/UserWalletDTO";
import AuthUtil from "../../AuthUtil";
import UserWalletServiceUrlProvider from "./UserWalletServiceUrlProvider";
import ResponseDTO from "../../../dto/user/ResponseDTO";

export async function getUserWalletDetails(): Promise<ResponseDTO<UserWalletDTO | null>> {
    const url: string = UserWalletServiceUrlProvider.v2().wallet().provide();
    const jwt: string | null = AuthUtil.getToken();

    if (jwt === null) {
        return new ResponseDTO(false, null, "Cannot load wallet data. Reason: Unauthorized");
    }

    try {
        const res = await axios.get(url, {
            headers: {
                Authorization: `Bearer ${jwt}`
            }
        });
        return new ResponseDTO(true, res.data);
    } catch (err) {
        const errMsg = err instanceof Error ? err.message : "Unknown error";
        return new ResponseDTO(false, null, `Cannot load wallet data. Reason: ${errMsg}`);
    }
}

