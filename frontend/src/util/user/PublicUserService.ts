import React from "react";
import axios from "axios";
import {UserRegisterDTO} from "../../dto/user/UserRegisterDTO";
import {UserLoginDTO} from "../../dto/user/UserLoginDTO";
import AuthUtil from "../AuthUtil";
import RedirectUtil from "../RedirectUtil";
import UserServiceUrlProvider from "./UserServiceUrlProvider";

export default class PublicUserService {

    private static readonly REDIRECT_URL_AFTER_SIGN_UP_SUCCESS: string = "/user/login?fromSignUp=true";

    private static readonly REDIRECT_URL_AFTER_SIGN_IN_SUCCESS: string = "/";

    static registerUser(userRegister: UserRegisterDTO, setErrMsg: React.Dispatch<React.SetStateAction<string>>): void {
        const registerUrl: string = UserServiceUrlProvider.v1().register().provide();

        axios.post(registerUrl, userRegister)
            .then(() => {
                RedirectUtil.redirectTo(this.REDIRECT_URL_AFTER_SIGN_UP_SUCCESS);
            })
            .catch((err) => {
                if (err.response === undefined) {
                    setErrMsg(err.message);
                } else {
                    setErrMsg(err.response.data.message);
                }
            });
    }

    static logInUser(userLogin: UserLoginDTO, setErrMsg: React.Dispatch<React.SetStateAction<string>>): void {
        const loginUrl: string = UserServiceUrlProvider.v1().login().provide();

        axios.post(loginUrl, userLogin)
            .then((res) => {
                AuthUtil.logIn(userLogin.username, res.data.token);
                RedirectUtil.redirectTo(this.REDIRECT_URL_AFTER_SIGN_IN_SUCCESS);
            })
            .catch((err) => {
                if (err.response === undefined) {
                    setErrMsg(err.message);
                } else {
                    setErrMsg(err.response.data.message);
                }
            });
    }
}
