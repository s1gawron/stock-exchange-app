import ResponseDTO from "../../dto/user/ResponseDTO.ts";
import {TransactionRequestDTO} from "../../dto/transaction/TransactionRequestDTO.ts";
import {createTransactionUrl} from "./TransactionServiceUrlProvider.ts";
import authenticatedApiClient from "../AuthenticatedApiClient.ts";
import getErrMsg from "../ErrorMsgProvider.ts";

export async function createTransaction(transactionRequestDTO: TransactionRequestDTO): Promise<ResponseDTO<string | null>> {
    const transactionCreateUrl = createTransactionUrl();

    try {
        await authenticatedApiClient.post(transactionCreateUrl, transactionRequestDTO);

        return new ResponseDTO(true, "OK");
    } catch (err) {
        const errMsg = getErrMsg(err);
        return new ResponseDTO(false, null, errMsg);
    }
}
