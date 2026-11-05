import {errorResponse, ResponseDTO, successResponse} from "../../dto/ResponseDTO.ts";
import {TransactionRequestDTO} from "../../dto/transaction/TransactionRequestDTO.ts";
import {TransactionResultDTO} from "../../dto/transaction/TransactionResultDTO.ts";
import {TransactionModifyRequestDTO} from "../../dto/transaction/TransactionModifyRequestDTO.ts";
import {cancelTransactionUrl, createTransactionUrl, getTransactionsUrl, modifyTransactionUrl} from "./TransactionServiceUrlProvider.ts";
import authenticatedApiClient from "../AuthenticatedApiClient.ts";
import getErrMsg from "../ErrorMsgProvider.ts";

export async function createTransaction(transactionRequestDTO: TransactionRequestDTO): Promise<ResponseDTO<TransactionResultDTO>> {
    try {
        const res = await authenticatedApiClient.post<TransactionResultDTO>(createTransactionUrl(), transactionRequestDTO);
        return successResponse(res.data);
    } catch (err) {
        const errMsg = getErrMsg(err);
        return errorResponse(errMsg);
    }
}

export async function getTransactions(): Promise<ResponseDTO<TransactionResultDTO[]>> {
    try {
        const res = await authenticatedApiClient.get<TransactionResultDTO[]>(getTransactionsUrl());
        return successResponse(res.data);
    } catch (err) {
        const errMsg = getErrMsg(err);
        return errorResponse(errMsg);
    }
}

export async function cancelTransaction(transactionId: number): Promise<ResponseDTO<TransactionResultDTO>> {
    try {
        const res = await authenticatedApiClient.post<TransactionResultDTO>(cancelTransactionUrl(transactionId));
        return successResponse(res.data);
    } catch (err) {
        const errMsg = getErrMsg(err);
        return errorResponse(errMsg);
    }
}

/**
 * The backend cancels the transaction and creates a new one, so the returned transaction has a new id.
 */
export async function modifyTransaction(transactionId: number, transactionModifyRequestDTO: TransactionModifyRequestDTO):
    Promise<ResponseDTO<TransactionResultDTO>> {
    try {
        const res = await authenticatedApiClient.put<TransactionResultDTO>(modifyTransactionUrl(transactionId), transactionModifyRequestDTO);
        return successResponse(res.data);
    } catch (err) {
        const errMsg = getErrMsg(err);
        return errorResponse(errMsg);
    }
}
