import ResponseDTO from "../../dto/user/ResponseDTO.ts";
import {TransactionRequestDTO} from "../../dto/transaction/TransactionRequestDTO.ts";
import {createTransactionUrl} from "./TransactionServiceUrlProvider.ts";
import axios from "axios";
import getErrMsg from "../ErrorMsgProvider.ts";
import {getToken} from "../AuthUtil.ts";

export async function createTransaction(transactionRequestDTO: TransactionRequestDTO): Promise<ResponseDTO<string | null>> {
    const transactionCreateUrl = createTransactionUrl();
    const jwt = getToken();

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
