import axios from "axios";
import {UserRegisterDTO} from "../../dto/user/UserRegisterDTO.ts";
import {UserLoginDTO} from "../../dto/user/UserLoginDTO.ts";
import UserServiceUrlProvider from "./UserServiceUrlProvider.ts";
import ResponseDTO from "../../dto/user/ResponseDTO.ts";
import {UserLoginResponseDTO} from "../../dto/user/UserLoginResponseDTO.ts";
import getErrMsg from "../ErrorMsgProvider.ts";

export async function registerUser(userRegister: UserRegisterDTO): Promise<ResponseDTO<string | null>> {
    const registerUrl = UserServiceUrlProvider.v1().register().provide();

    try {
        await axios.post(registerUrl, userRegister);
        return new ResponseDTO(true, "OK");
    } catch (err) {
        const errMsg = getErrMsg(err);
        return new ResponseDTO(false, null, errMsg);
    }
}

export async function logInUser(userLogin: UserLoginDTO): Promise<ResponseDTO<UserLoginResponseDTO | null>> {
    const loginUrl = UserServiceUrlProvider.v1().login().provide();

    try {
        const res = await axios.post(loginUrl, userLogin);
        const body: UserLoginResponseDTO = {
            username: userLogin.username,
            token: res.data.token
        };

        return new ResponseDTO(true, body);
    } catch (err) {
        const errMsg = getErrMsg(err);
        return new ResponseDTO(false, null, errMsg);
    }
}
