import React from "react";
import axios from "axios";
import {UserRegisterDTO} from "../dto/UserRegisterDTO";

const BASE_URL: string = "http://localhost:8080"
const USER_REGISTER_ENDPOINT: string = "/api/public/user/v1/register";
const REDIRECT_URL_AFTER_SIGN_UP_SUCCESS: string = "/user/login?fromSignUp=true";

export function registerUser(userRegister: UserRegisterDTO, setErrMsg: React.Dispatch<React.SetStateAction<string>>): void {
    const registerUri: string = BASE_URL + USER_REGISTER_ENDPOINT;

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