import React, {useEffect, useRef, useState} from 'react';
import styles from "./styles.module.css";
import {TransactionType} from "../../../dto/transaction/TransactionRequestDTO.ts";
import {createTransaction} from "../../../util/transaction/TransactionService.ts";
import {useNavigate} from "react-router-dom";
import {useToast} from "../../toast/ToastContext.ts";
import {UNKNOWN_ERROR_MSG} from "../../../util/ErrorMsgProvider.ts";
import {getUserStocks} from "../../../util/user/wallet/UserWalletService.ts";
import {UserStockPositionDTO} from "../../../dto/user/UserStockPositionDTO.ts";
import {formatQuantity} from "../../../util/QuantityFormatter.ts";

const REDIRECT_URL_AFTER_CREATE_TRANSACTION_SUCCESS: string = "/user/wallet";
const PRICE_STEP: string = "0.0001";
const QUANTITY_STEP: string = "0.000001";
const FIFO_POSITION: string = "";

export default function TransactionForm({ticker}: { ticker: string }): React.ReactElement {
    const [values, setValues] = useState({price: "", quantity: "", positionId: FIFO_POSITION});
    const [positions, setPositions] = useState<UserStockPositionDTO[]>([]);

    const {showError} = useToast();
    const [submitting, setSubmitting] = useState<boolean>(false);
    const formRef = useRef<HTMLFormElement>(null);
    const navigate = useNavigate();

    useEffect(() => {
        getUserStocks().then(res => {
            if (res.success) {
                const userStock = res.responseBody.find(stock => stock.ticker === ticker);
                setPositions(userStock?.positions.filter(position => position.quantity > 0) ?? []);
                return;
            }

            showError(res.errorMsg);
        }).catch((error) => {
            console.error("An unexpected error occurred while loading user stock positions:", error);
            showError(UNKNOWN_ERROR_MSG);
        });
    }, [showError, ticker]);

    const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
        const {name, value} = e.target;
        setValues(prev => ({...prev, [name]: value}));
    };

    const handleSubmit = (type: TransactionType) => (e: React.MouseEvent<HTMLButtonElement>) => {
        e.preventDefault();

        if (submitting || !formRef.current!.reportValidity()) {
            return;
        }

        setSubmitting(true);

        const positionId = type === TransactionType.SELL && values.positionId !== FIFO_POSITION ? Number(values.positionId) : undefined;

        createTransaction({type, stockTicker: ticker, price: Number(values.price), quantity: Number(values.quantity), positionId}).then(res => {
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
                    <input type="number" name="quantity" placeholder="Quantity" min={QUANTITY_STEP} step={QUANTITY_STEP} value={values.quantity}
                           onChange={handleChange} className={styles.textInput} required/>
                </div>

                {positions.length > 0 && (
                    <div className={styles.formElementWrapper}>
                        <select name="positionId" value={values.positionId} onChange={handleChange} className={styles.textInput}
                                aria-label="Position to sell">
                            <option value={FIFO_POSITION}>Sell from oldest positions first</option>
                            {positions.map(position => (
                                <option key={position.id} value={position.id}>
                                    {new Date(position.openDate).toLocaleDateString()}: {formatQuantity(position.quantity)} @ {position.openPrice.toFixed(4)}
                                </option>
                            ))}
                        </select>
                    </div>
                )}

                <div id={styles.transactionBtnWrapper}>
                    <button id={styles.sellBtn} className={styles.transactionBtn} onClick={handleSubmit(TransactionType.SELL)} disabled={submitting}>Sell</button>
                    <button id={styles.purchaseBtn} className={styles.transactionBtn} onClick={handleSubmit(TransactionType.PURCHASE)}
                            disabled={submitting}>Purchase</button>
                </div>
            </form>
        </div>
    );
}
