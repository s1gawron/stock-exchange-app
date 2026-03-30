import React, {useState} from 'react';
import styles from "./styles.module.css";
import {TransactionType} from "../../../dto/transaction/TransactionRequestDTO.ts";
import {createTransaction} from "../../../util/transaction/TransactionService.ts";
import {useNavigate} from "react-router-dom";
import ErrorMsg from "../../error/ErrorMsg.tsx";

const REDIRECT_URL_AFTER_CREATE_TRANSACTION_SUCCESS: string = "/user/wallet";

export default function TransactionForm({ticker}: { ticker: string }): React.ReactElement {
    const [values, setValues] = useState({
        stockTicker: ticker,
        price: undefined as number | undefined,
        quantity: undefined as number | undefined,
    });

    const [errMsg, setErrMsg] = useState<string>("");
    const navigate = useNavigate();

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const {name, value} = e.target;
        setValues(prev => ({...prev, [name]: parseFloat(value)}));
    };

    const handleSubmit = (type: TransactionType) => (e: React.MouseEvent<HTMLButtonElement>) => {
        e.preventDefault();

        createTransaction({type, ...values}).then(res => {
            if (res.success) {
                navigate(REDIRECT_URL_AFTER_CREATE_TRANSACTION_SUCCESS);
                setErrMsg("");
                return;
            }

            setErrMsg(res.errorMsg!);
        }).catch((error) => {
            console.error("An unexpected error occurred while creating transaction:", error);
        });
    };

    return (
        <div id={styles.formWrapper}>
            <div id={styles.errWrapper}>
                <ErrorMsg errMsg={errMsg}/>
            </div>

            <form>
                <div className={styles.formElementWrapper}>
                    <input type="number" name="price" placeholder="Price" value={values.price} onChange={handleChange} className={styles.textInput} required/>
                </div>

                <div className={styles.formElementWrapper}>
                    <input type="number" name="quantity" placeholder="Quantity" value={values.quantity} onChange={handleChange} className={styles.textInput}
                           required/>
                </div>

                <div id={styles.transactionBtnWrapper}>
                    <button id={styles.sellBtn} className={styles.transactionBtn} onClick={handleSubmit(TransactionType.SELL)}>Sell</button>
                    <button id={styles.purchaseBtn} className={styles.transactionBtn} onClick={handleSubmit(TransactionType.PURCHASE)}>Purchase</button>
                </div>
            </form>
        </div>
    );
}
