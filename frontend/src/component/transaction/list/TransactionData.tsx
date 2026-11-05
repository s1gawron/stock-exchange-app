import React from "react";
import styles from "./styles.module.css";
import {TransactionResultDTO} from "../../../dto/transaction/TransactionResultDTO.ts";
import TransactionRow from "./TransactionRow.tsx";

export default function TransactionData({transactions, onTransactionsChanged}: {
    transactions: TransactionResultDTO[],
    onTransactionsChanged: () => void
}): React.ReactElement {
    if (transactions.length === 0) {
        return <div id={styles.noTransactions}>You have no orders yet.</div>;
    }

    return (
        <fieldset>
            <legend>Your orders:</legend>
            <div className={styles.tableWrapper}>
                <table id={styles.transactionsTable}>
                    <thead>
                    <tr>
                        <th className={`${styles.cell} ${styles.th}`}>Created</th>
                        <th className={`${styles.cell} ${styles.th}`}>Ticker</th>
                        <th className={`${styles.cell} ${styles.th}`}>Type</th>
                        <th className={`${styles.cell} ${styles.th}`}>Status</th>
                        <th className={`${styles.cell} ${styles.th}`}>Price limit</th>
                        <th className={`${styles.cell} ${styles.th}`}>Filled / quantity</th>
                        <th className={`${styles.cell} ${styles.th}`}>Average fill price</th>
                        <th className={`${styles.cell} ${styles.th}`}>Balance blocked</th>
                        <th className={`${styles.cell} ${styles.th}`}>Actions</th>
                    </tr>
                    </thead>

                    <tbody>
                    {transactions.map(transaction => (
                        <TransactionRow key={transaction.transactionId} transaction={transaction} onTransactionChanged={onTransactionsChanged}/>
                    ))}
                    </tbody>
                </table>
            </div>
        </fieldset>
    );
}
