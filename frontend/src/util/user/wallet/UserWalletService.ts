import axios from "axios";
import React from "react";
import {UserWalletDTO} from "../../../dto/user/UserWalletDTO";
import AuthUtil from "../../AuthUtil";
import UserWalletServiceUrlProvider from "./UserWalletServiceUrlProvider";

export default class UserWalletService {

    static getUserWalletDetails(setUserWallet: React.Dispatch<React.SetStateAction<UserWalletDTO>>): void {
        const url: string = UserWalletServiceUrlProvider.v2().wallet().provide();
        const jwt: string | null = AuthUtil.getToken();

        if (jwt === null) {
            return;
        }

        axios.get(url, {
            headers: {
                Authorization: `Bearer ${jwt}`
            }
        })
            .then((res) => setUserWallet(res.data))
            .catch((err) => console.log(err));
    }

}
