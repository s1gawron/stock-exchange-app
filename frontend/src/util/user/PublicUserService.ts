import axios from "axios";
import {UserRegisterDTO} from "../../dto/user/UserRegisterDTO.ts";
import {UserLoginDTO} from "../../dto/user/UserLoginDTO.ts";
import {loginUrl, registerUrl} from "./UserServiceUrlProvider.ts";
import ResponseDTO from "../../dto/user/ResponseDTO.ts";
import {UserLoginResponseDTO} from "../../dto/user/UserLoginResponseDTO.ts";
import getErrMsg from "../ErrorMsgProvider.ts";

export async function registerUser(userRegister: UserRegisterDTO): Promise<ResponseDTO<string | null>> {
    const url = registerUrl();

    try {
        await axios.post(url, userRegister);
        return new ResponseDTO(true, "OK");
    } catch (err) {
        const errMsg = getErrMsg(err);
        return new ResponseDTO(false, null, errMsg);
    }
}

export async function logInUser(userLogin: UserLoginDTO): Promise<ResponseDTO<UserLoginResponseDTO | null>> {
    const url = loginUrl();

    try {
        const res = await axios.post(url, userLogin);
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
