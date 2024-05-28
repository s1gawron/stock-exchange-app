import React from "react";
import axios from "axios";
import {UserRegisterDTO} from "../dto/UserRegisterDTO";
import {UserLoginDTO} from "../dto/UserLoginDTO";

const BASE_URL: string = "http://localhost:8080/api/public/user";
const CURRENT_API_VERSION: string = "/v1"
const USER_REGISTER_ENDPOINT: string = "/register";
const USER_LOGIN_ENDPOINT: string = "/login";

const REDIRECT_URL_AFTER_SIGN_UP_SUCCESS: string = "/user/login?fromSignUp=true";
const REDIRECT_URL_AFTER_SIGN_IN_SUCCESS: string = "/";

export function registerUser(userRegister: UserRegisterDTO, setErrMsg: React.Dispatch<React.SetStateAction<string>>): void {
    const registerUri: string = BASE_URL + CURRENT_API_VERSION + USER_REGISTER_ENDPOINT;

    axios.post(registerUri, userRegister)
        .then(() => {
            window.location.assign(REDIRECT_URL_AFTER_SIGN_UP_SUCCESS);
        })
        .catch((err) => {
            if (err.response === undefined) {
                setErrMsg(err.message);
            } else {
                setErrMsg(err.response.data.message);
            }
        });
}

export function loginUser(userLogin: UserLoginDTO, setErrMsg: React.Dispatch<React.SetStateAction<string>>): void {
    const loginUri: string = BASE_URL + CURRENT_API_VERSION + USER_LOGIN_ENDPOINT;

    axios.post(loginUri, userLogin)
        .then((res) => {
            localStorage.setItem('username', userLogin.username);
            localStorage.setItem('token', res.headers.authorization);

            window.location.assign(REDIRECT_URL_AFTER_SIGN_IN_SUCCESS);
        })
        .catch((err) => {
            if (err.response === undefined) {
                setErrMsg(err.message);
            } else if (err.response.status === 401) {
                setErrMsg("Bad credentials provided!");
            } else {
                setErrMsg(err.response.data.message);
            }
        });
}