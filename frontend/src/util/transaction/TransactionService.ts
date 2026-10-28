import {errorResponse, ResponseDTO, successResponse} from "../../dto/ResponseDTO.ts";
import {TransactionRequestDTO} from "../../dto/transaction/TransactionRequestDTO.ts";
import {createTransactionUrl} from "./TransactionServiceUrlProvider.ts";
import authenticatedApiClient from "../AuthenticatedApiClient.ts";
import getErrMsg from "../ErrorMsgProvider.ts";

export async function createTransaction(transactionRequestDTO: TransactionRequestDTO): Promise<ResponseDTO<void>> {
    const transactionCreateUrl = createTransactionUrl();

    try {
        await authenticatedApiClient.post(transactionCreateUrl, transactionRequestDTO);

        return successResponse(undefined);
    } catch (err) {
        const errMsg = getErrMsg(err);
        return errorResponse(errMsg);
    }
}
