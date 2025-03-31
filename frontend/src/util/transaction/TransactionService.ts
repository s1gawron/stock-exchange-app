import ResponseDTO from "../../dto/user/ResponseDTO.ts";
import {TransactionRequestDTO} from "../../dto/transaction/TransactionRequestDTO.ts";
import TransactionServiceUrlProvider from "./TransactionServiceUrlProvider.ts";
import axios from "axios";
import getErrMsg from "../ErrorMsgProvider.ts";
import AuthUtil from "../AuthUtil.ts";

export async function createTransaction(transactionRequestDTO: TransactionRequestDTO): Promise<ResponseDTO<string | null>> {
    const transactionCreateUrl = TransactionServiceUrlProvider.v1().createTransaction().provide();
    const jwt = AuthUtil.getToken();

    if (jwt === null) {
        return new ResponseDTO(false, null, "Unauthorized");
    }

    try {
        await axios.post(transactionCreateUrl, transactionRequestDTO, {
            headers: {
                Authorization: `Bearer ${jwt}`
            }
        });

        return new ResponseDTO(true, "OK");
    } catch (err) {
        const errMsg = getErrMsg(err);
        return new ResponseDTO(false, null, errMsg);
    }
}