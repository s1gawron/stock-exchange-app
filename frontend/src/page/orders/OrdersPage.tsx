import React, {useCallback, useEffect, useRef, useState} from "react";
import Topbar from "../../component/topbar/Topbar.tsx";
import Footer from "../../component/footer/Footer.tsx";
import styles from "./styles.module.css";
import {useToast} from "../../component/toast/ToastContext.ts";
import {UNKNOWN_ERROR_MSG} from "../../util/ErrorMsgProvider.ts";
import {getTransactions} from "../../util/transaction/TransactionService.ts";
import {isTransactionOpen, TransactionResultDTO, TransactionStatus} from "../../dto/transaction/TransactionResultDTO.ts";
import TransactionData from "../../component/transaction/list/TransactionData.tsx";
import {formatQuantity} from "../../util/QuantityFormatter.ts";

const POLLING_INTERVAL_MS: number = 3000;

export default function OrdersPage(): React.ReactElement {
    const {showError, showSuccess} = useToast();
    const [transactions, setTransactions] = useState<TransactionResultDTO[]>();
    const [loading, setLoading] = useState<boolean>(true);
    const requestInProgress = useRef<boolean>(false);
    const previousStatuses = useRef<Map<number, TransactionStatus>>(new Map());

    const notifyAboutFills = useCallback((loadedTransactions: TransactionResultDTO[]) => {
        for (const transaction of loadedTransactions) {
            const previousStatus = previousStatuses.current.get(transaction.transactionId);

            if (previousStatus !== undefined && previousStatus !== TransactionStatus.FILLED && transaction.transactionStatus === TransactionStatus.FILLED) {
                showSuccess(`Order #${transaction.transactionId} filled: ${formatQuantity(transaction.stockQuantity)} ${transaction.stockTicker}.`);
            }
        }

        previousStatuses.current = new Map(loadedTransactions.map(transaction => [transaction.transactionId, transaction.transactionStatus]));
    }, [showSuccess]);

    // Errors of background refreshes are only logged, so a short network problem does not show a toast every few seconds.
    const loadTransactions = useCallback((showErrors: boolean) => {
        if (requestInProgress.current) {
            return;
        }

        requestInProgress.current = true;

        getTransactions().then(res => {
            if (res.success) {
                notifyAboutFills(res.responseBody);
                setTransactions(res.responseBody);
            } else if (showErrors) {
                showError(res.errorMsg);
            } else {
                console.error("Could not refresh orders:", res.errorMsg);
            }
        }).catch((error) => {
            console.error("An unexpected error occurred while loading orders:", error);

            if (showErrors) {
                showError(UNKNOWN_ERROR_MSG);
            }
        }).finally(() => {
            requestInProgress.current = false;
            setLoading(false);
        });
    }, [notifyAboutFills, showError]);

    useEffect(() => {
        loadTransactions(true);
    }, [loadTransactions]);

    const hasOpenTransactions = transactions?.some(transaction => isTransactionOpen(transaction.transactionStatus)) ?? false;

    useEffect(() => {
        if (!hasOpenTransactions) {
            return;
        }

        const intervalId = window.setInterval(() => {
            if (document.visibilityState === "visible") {
                loadTransactions(false);
            }
        }, POLLING_INTERVAL_MS);

        return () => window.clearInterval(intervalId);
    }, [hasOpenTransactions, loadTransactions]);

    return (
        <>
            <Topbar/>

            <div id={styles.container}>
                {loading ? (
                    <div className={styles.loaderWrapper}>
                        <div className={styles.loader}></div>
                    </div>
                ) : transactions !== undefined && (
                    <TransactionData transactions={transactions} onTransactionsChanged={() => loadTransactions(true)}/>
                )}

                {hasOpenTransactions && <p id={styles.refreshInfo}>Open orders are refreshed every {POLLING_INTERVAL_MS / 1000} seconds.</p>}
            </div>

            <Footer text="Patience pays off."/>
        </>
    );
}
