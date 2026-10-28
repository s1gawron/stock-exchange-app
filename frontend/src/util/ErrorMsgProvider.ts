import {AxiosError} from "axios";

export const UNKNOWN_ERROR_MSG: string = "Unknown error occurred!";

export default function getErrMsg(error: unknown): string {
    if (error instanceof AxiosError) {
        return error.response?.data?.message ?? error.message;
    } else {
        return UNKNOWN_ERROR_MSG;
    }

}