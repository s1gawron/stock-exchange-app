import axios from "axios";
import {UserRegisterDTO} from "../../dto/user/UserRegisterDTO";
import {UserLoginDTO} from "../../dto/user/UserLoginDTO";
import UserServiceUrlProvider from "./UserServiceUrlProvider";
import ResponseDTO from "../../dto/user/ResponseDTO";
import {UserLoginResponseDTO} from "../../dto/user/UserLoginResponseDTO";
import getErrMsg from "../ErrorMsgProvider";

export async function registerUser(userRegister: UserRegisterDTO): Promise<ResponseDTO<string | null>> {
    const registerUrl: string = UserServiceUrlProvider.v1().register().provide();

    try {
        await axios.post(registerUrl, userRegister);
        return new ResponseDTO(true, "OK");
    } catch (err) {
        const errMsg = getErrMsg(err);
        return new ResponseDTO(false, null, errMsg);
    }
}

export async function logInUser(userLogin: UserLoginDTO): Promise<ResponseDTO<UserLoginResponseDTO | null>> {
    const loginUrl: string = UserServiceUrlProvider.v1().login().provide();

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
