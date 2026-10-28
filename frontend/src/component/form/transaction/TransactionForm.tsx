import React, {useRef, useState} from 'react';
import styles from "./styles.module.css";
import {TransactionType} from "../../../dto/transaction/TransactionRequestDTO.ts";
import {createTransaction} from "../../../util/transaction/TransactionService.ts";
import {useNavigate} from "react-router-dom";
import {useToast} from "../../toast/ToastContext.ts";
import {UNKNOWN_ERROR_MSG} from "../../../util/ErrorMsgProvider.ts";

const REDIRECT_URL_AFTER_CREATE_TRANSACTION_SUCCESS: string = "/user/wallet";
const PRICE_STEP: string = "0.0001";
const QUANTITY_STEP: string = "1";

export default function TransactionForm({ticker}: { ticker: string }): React.ReactElement {
    const [values, setValues] = useState({price: "", quantity: ""});

    const {showError} = useToast();
    const [submitting, setSubmitting] = useState<boolean>(false);
    const formRef = useRef<HTMLFormElement>(null);
    const navigate = useNavigate();

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const {name, value} = e.target;
        setValues(prev => ({...prev, [name]: value}));
    };

    const handleSubmit = (type: TransactionType) => (e: React.MouseEvent<HTMLButtonElement>) => {
        e.preventDefault();

        if (submitting || !formRef.current!.reportValidity()) {
            return;
        }

        setSubmitting(true);

        createTransaction({type, stockTicker: ticker, price: Number(values.price), quantity: Number(values.quantity)}).then(res => {
            if (res.success) {
                navigate(REDIRECT_URL_AFTER_CREATE_TRANSACTION_SUCCESS);
                return;
            }

            showError(res.errorMsg);
            setSubmitting(false);
        }).catch((error) => {
            console.error("An unexpected error occurred while creating transaction:", error);
            showError(UNKNOWN_ERROR_MSG);
            setSubmitting(false);
        });
    };

    return (
        <div id={styles.formWrapper}>
            <form ref={formRef}>
                <div className={styles.formElementWrapper}>
                    <input type="number" name="price" placeholder="Price" min={PRICE_STEP} step={PRICE_STEP} value={values.price} onChange={handleChange}
                           className={styles.textInput} required/>
                </div>

                <div className={styles.formElementWrapper}>
                    <input type="number" name="quantity" placeholder="Quantity" min={QUANTITY_STEP} step={QUANTITY_STEP} value={values.quantity} onChange={handleChange}
                           className={styles.textInput} required/>
                </div>

                <div id={styles.transactionBtnWrapper}>
                    <button id={styles.sellBtn} className={styles.transactionBtn} onClick={handleSubmit(TransactionType.SELL)} disabled={submitting}>Sell</button>
                    <button id={styles.purchaseBtn} className={styles.transactionBtn} onClick={handleSubmit(TransactionType.PURCHASE)}
                            disabled={submitting}>Purchase</button>
                </div>
            </form>
        </div>
    );
}
