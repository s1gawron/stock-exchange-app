import axios from "axios";
import {UserRegisterDTO} from "../../dto/user/UserRegisterDTO.ts";
import {UserLoginDTO} from "../../dto/user/UserLoginDTO.ts";
import {loginUrl, registerUrl} from "./UserServiceUrlProvider.ts";
import {errorResponse, ResponseDTO, successResponse} from "../../dto/ResponseDTO.ts";
import {UserLoginResponseDTO} from "../../dto/user/UserLoginResponseDTO.ts";
import getErrMsg from "../ErrorMsgProvider.ts";

export async function registerUser(userRegister: UserRegisterDTO): Promise<ResponseDTO<void>> {
    const url = registerUrl();

    try {
        await axios.post(url, userRegister);
        return successResponse(undefined);
    } catch (err) {
        const errMsg = getErrMsg(err);
        return errorResponse(errMsg);
    }
}

export async function logInUser(userLogin: UserLoginDTO): Promise<ResponseDTO<UserLoginResponseDTO>> {
    const url = loginUrl();

    try {
        const res = await axios.post<{ token: string }>(url, userLogin);
        const body: UserLoginResponseDTO = {
            username: userLogin.username,
            token: res.data.token
        };

        return successResponse(body);
    } catch (err) {
        const errMsg = getErrMsg(err);
        return errorResponse(errMsg);
    }
}
