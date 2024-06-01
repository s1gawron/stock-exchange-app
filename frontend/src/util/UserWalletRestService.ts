import axios from "axios";
import React from "react";
import {UserWalletDTO} from "../dto/user/UserWalletDTO";

const BASE_URL: string = "http://localhost:8080/api/user";
const CURRENT_API_VERSION: string = "/v2";
const WALLET_DETAILS_ENDPOINT: string = "/wallet";

export function getUserWalletDetails(setUserWallet: React.Dispatch<React.SetStateAction<UserWalletDTO>>): void {
    const walletDetailsUri: string = BASE_URL + CURRENT_API_VERSION + WALLET_DETAILS_ENDPOINT;
    const jwt: string | null = localStorage.getItem("token");

    if (jwt === null) {
        return;
    }

    axios.get(walletDetailsUri, {
        headers: {
            Authorization: `Bearer ${jwt}`
        }
    })
        .then((res) => setUserWallet(res.data))
        .catch((err) => console.log(err));
}
