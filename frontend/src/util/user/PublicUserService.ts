import React from "react";
import axios, {AxiosError} from "axios";
import {UserRegisterDTO} from "../../dto/user/UserRegisterDTO";
import {UserLoginDTO} from "../../dto/user/UserLoginDTO";
import AuthUtil from "../AuthUtil";
import RedirectUtil from "../RedirectUtil";
import UserServiceUrlProvider from "./UserServiceUrlProvider";
import ResponseDTO from "../../dto/user/ResponseDTO";

const REDIRECT_URL_AFTER_SIGN_IN_SUCCESS: string = "/";

export async function registerUser(userRegister: UserRegisterDTO): Promise<ResponseDTO<string | null>> {
    const registerUrl: string = UserServiceUrlProvider.v1().register().provide();

    try {
        await axios.post(registerUrl, userRegister);
        return new ResponseDTO(true, "OK");
    } catch (err) {
        let errMsg: string;

        if (err instanceof AxiosError) {
            errMsg = err.response === undefined ? err.message : err.response.data.message;
        } else {
            errMsg = "Unknown error";
        }

        return new ResponseDTO(false, null, `Cannot register user. Reason: ${errMsg}`);
    }
}

export function logInUser(userLogin: UserLoginDTO, setErrMsg: React.Dispatch<React.SetStateAction<string>>): void {
    const loginUrl: string = UserServiceUrlProvider.v1().login().provide();

    axios.post(loginUrl, userLogin)
        .then((res) => {
            AuthUtil.logIn(userLogin.username, res.data.token);
            RedirectUtil.redirectTo(REDIRECT_URL_AFTER_SIGN_IN_SUCCESS);
        })
        .catch((err) => {
            if (err.response === undefined) {
                setErrMsg(err.message);
            } else {
                setErrMsg(err.response.data.message);
            }
        });
}
