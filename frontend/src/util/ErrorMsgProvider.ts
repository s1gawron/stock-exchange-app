import {AxiosError} from "axios";

export default function getErrMsg(error: unknown) {
    if (error instanceof AxiosError) {
        return error.response?.data?.message ?? error.message;
    } else {
        return "Unknown error occurred!";
    }

}