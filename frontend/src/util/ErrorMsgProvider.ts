import {AxiosError} from "axios";

export default function getErrMsg(error: unknown) {
    if (error instanceof AxiosError) {
        return error.response === undefined ? error.message : error.response.data.message;
    } else {
        return "Unknown error occurred!";
    }

}