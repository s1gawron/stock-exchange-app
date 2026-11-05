import React, {useState} from "react";
import styles from "./styles.module.css";
import {isTransactionOpen, TransactionResultDTO, TransactionStatus} from "../../../dto/transaction/TransactionResultDTO.ts";
import {TransactionType} from "../../../dto/transaction/TransactionRequestDTO.ts";
import {formatQuantity} from "../../../util/QuantityFormatter.ts";
import {formatServerDate} from "../../../util/DateFormatter.ts";
import {cancelTransaction, modifyTransaction} from "../../../util/transaction/TransactionService.ts";
import {useToast} from "../../toast/ToastContext.ts";
import {UNKNOWN_ERROR_MSG} from "../../../util/ErrorMsgProvider.ts";

const COLUMN_COUNT: number = 9;
const PRICE_STEP: string = "0.0001";
const QUANTITY_STEP: string = "0.000001";

const STATUS_LABELS: Record<TransactionStatus, string> = {
    [TransactionStatus.NEW]: "New",
    [TransactionStatus.PARTIALLY_FILLED]: "Partially filled",
    [TransactionStatus.FILLED]: "Filled",
    [TransactionStatus.CANCELED]: "Canceled"
};

const STATUS_CLASSES: Record<TransactionStatus, string> = {
    [TransactionStatus.NEW]: styles.statusNew,
    [TransactionStatus.PARTIALLY_FILLED]: styles.statusPartiallyFilled,
    [TransactionStatus.FILLED]: styles.statusFilled,
    [TransactionStatus.CANCELED]: styles.statusCanceled
};

function formatMoney(value: number | null, currency: string): string {
    return value === null ? "-" : `${value.toFixed(4)} ${currency}`;
}

export default function TransactionRow({transaction, onTransactionChanged}: {
    transaction: TransactionResultDTO,
    onTransactionChanged: () => void
}): React.ReactElement {
    const [detailsVisible, setDetailsVisible] = useState<boolean>(false);
    const [submitting, setSubmitting] = useState<boolean>(false);
    const [modifyValues, setModifyValues] = useState({
        price: transaction.stockPriceLimit.toString(),
        quantity: formatQuantity(transaction.remainingQuantity)
    });
    const {showError, showSuccess} = useToast();

    const open = isTransactionOpen(transaction.transactionStatus);
    const isPurchase = transaction.transactionType === TransactionType.PURCHASE;
    const currency = transaction.stockCurrency;

    const handleCancel = () => {
        if (submitting || !window.confirm(`Cancel order #${transaction.transactionId} for ${transaction.stockTicker}?`)) {
            return;
        }

        setSubmitting(true);

        cancelTransaction(transaction.transactionId).then(res => {
            if (res.success) {
                showSuccess(`Order #${transaction.transactionId} canceled.`);
                onTransactionChanged();
            } else {
                showError(res.errorMsg);
            }

            setSubmitting(false);
        }).catch((error) => {
            console.error("An unexpected error occurred while canceling transaction:", error);
            showError(UNKNOWN_ERROR_MSG);
            setSubmitting(false);
        });
    };

    const handleModify = (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();

        if (submitting) {
            return;
        }

        setSubmitting(true);

        modifyTransaction(transaction.transactionId, {price: Number(modifyValues.price), quantity: Number(modifyValues.quantity)}).then(res => {
            if (res.success) {
                showSuccess(`Order #${transaction.transactionId} replaced by order #${res.responseBody.transactionId}.`);
                onTransactionChanged();
            } else {
                showError(res.errorMsg);
            }

            setSubmitting(false);
        }).catch((error) => {
            console.error("An unexpected error occurred while modifying transaction:", error);
            showError(UNKNOWN_ERROR_MSG);
            setSubmitting(false);
        });
    };

    const handleModifyChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const {name, value} = e.target;
        setModifyValues(prev => ({...prev, [name]: value}));
    };

    return (
        <>
            <tr>
                <td className={`${styles.cell} ${styles.td}`} data-label="Created">{formatServerDate(transaction.transactionCreateDate)}</td>
                <td className={`${styles.cell} ${styles.td}`} data-label="Ticker">{transaction.stockTicker}</td>
                <td className={`${styles.cell} ${styles.td} ${isPurchase ? styles.purchase : styles.sell}`} data-label="Type">
                    {isPurchase ? "Buy" : "Sell"}
                </td>
                <td className={`${styles.cell} ${styles.td} ${STATUS_CLASSES[transaction.transactionStatus]}`} data-label="Status">
                    {STATUS_LABELS[transaction.transactionStatus]}
                </td>
                <td className={`${styles.cell} ${styles.td}`} data-label="Price limit">{formatMoney(transaction.stockPriceLimit, currency)}</td>
                <td className={`${styles.cell} ${styles.td}`} data-label="Filled / qty">
                    {formatQuantity(transaction.filledQuantity)} / {formatQuantity(transaction.stockQuantity)}
                </td>
                <td className={`${styles.cell} ${styles.td}`} data-label="Avg fill price">{formatMoney(transaction.averageFillPrice, currency)}</td>
                <td className={`${styles.cell} ${styles.td}`} data-label="Blocked">{isPurchase ? formatMoney(transaction.balanceBlocked, currency) : "-"}</td>
                <td className={`${styles.cell} ${styles.td}`} data-label="Actions">
                    <div className={styles.actions}>
                        <button className={styles.actionButton} onClick={() => setDetailsVisible(prev => !prev)} aria-expanded={detailsVisible}>
                            {detailsVisible ? "Hide" : "Details"}
                        </button>
                        {open && (
                            <button className={`${styles.actionButton} ${styles.cancelButton}`} onClick={handleCancel} disabled={submitting}>
                                Cancel
                            </button>
                        )}
                    </div>
                </td>
            </tr>

            {detailsVisible && (
                <tr>
                    <td className={`${styles.cell} ${styles.detailsCell}`} colSpan={COLUMN_COUNT}>
                        <p className={styles.detailsText}>
                            Order #{transaction.transactionId}
                            {transaction.transactionProcessDate !== null && <>, closed {formatServerDate(transaction.transactionProcessDate)}</>}
                        </p>

                        <p className={styles.detailsTitle}>Fills</p>
                        {transaction.executions.length === 0 ? (
                            <p className={styles.detailsText}>No fills yet.</p>
                        ) : (
                            <table className={styles.executionsTable}>
                                <thead>
                                <tr>
                                    <th className={`${styles.cell} ${styles.th}`}>Date</th>
                                    <th className={`${styles.cell} ${styles.th}`}>Quantity</th>
                                    <th className={`${styles.cell} ${styles.th}`}>Price</th>
                                </tr>
                                </thead>
                                <tbody>
                                {transaction.executions.map((execution, index) => (
                                    <tr key={index}>
                                        <td className={`${styles.cell} ${styles.td}`} data-label="Date">{formatServerDate(execution.executionDate)}</td>
                                        <td className={`${styles.cell} ${styles.td}`} data-label="Quantity">{formatQuantity(execution.quantity)}</td>
                                        <td className={`${styles.cell} ${styles.td}`} data-label="Price">{formatMoney(execution.price, currency)}</td>
                                    </tr>
                                ))}
                                </tbody>
                            </table>
                        )}

                        {open && (
                            <>
                                <p className={styles.detailsTitle}>Modify</p>
                                <p className={styles.detailsText}>
                                    Modifying cancels this order and places a new one. Filled shares stay in this order, so enter the quantity that is still
                                    to be filled.
                                </p>
                                <form className={styles.modifyForm} onSubmit={handleModify}>
                                    <label className={styles.modifyLabel}>
                                        Price limit
                                        <input type="number" name="price" min={PRICE_STEP} step={PRICE_STEP} value={modifyValues.price}
                                               onChange={handleModifyChange} className={styles.modifyInput} required/>
                                    </label>
                                    <label className={styles.modifyLabel}>
                                        Quantity
                                        <input type="number" name="quantity" min={QUANTITY_STEP} step={QUANTITY_STEP} value={modifyValues.quantity}
                                               onChange={handleModifyChange} className={styles.modifyInput} required/>
                                    </label>
                                    <button type="submit" className={styles.actionButton} disabled={submitting}>Modify</button>
                                </form>
                            </>
                        )}
                    </td>
                </tr>
            )}
        </>
    );
}
