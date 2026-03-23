import React, {useState} from 'react';
import styles from "./styles.module.css";
import {TransactionRequestDTO, TransactionType} from "../../dto/transaction/TransactionRequestDTO.ts";

interface TransactionFormProps {
    ticker: string;
    onSubmit: (values: TransactionRequestDTO) => void;
}

export default function TransactionForm({ticker, onSubmit}: TransactionFormProps): React.ReactElement {
    const [values, setValues] = useState<TransactionRequestDTO>({
        type: TransactionType.PURCHASE,
        stockTicker: ticker,
        price: 0.00,
        quantity: 0,
    });

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const {name, value} = e.target;
        setValues(prev => ({...prev, [name]: value}));
    };

    const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        onSubmit(values);
    };

    return (
        <div id={styles.formWrapper}>
            <form onSubmit={handleSubmit}>
                <div>
                    <h4>Transaction type:</h4>
                    <div className={styles.radioOption}>
                        <input id={TransactionType.PURCHASE} type="radio" name="type" value={TransactionType.PURCHASE} checked={values.type === TransactionType.PURCHASE} onChange={handleChange}/>
                        <label htmlFor={TransactionType.PURCHASE}>Buy</label>
                    </div>
                    <div className={styles.radioOption}>
                        <input id={TransactionType.SELL} type="radio" name="type" value={TransactionType.SELL} checked={values.type === TransactionType.SELL} onChange={handleChange}/>
                        <label htmlFor={TransactionType.SELL}>Sell</label>
                    </div>
                </div>
                <div>
                    <h4>Price:</h4>
                    <input type="number" name="price" value={values.price} onChange={handleChange} className={styles.textInput} required/>
                </div>
                <div>
                    <h4>Quantity:</h4>
                    <input type="number" name="quantity" value={values.quantity} onChange={handleChange} className={styles.textInput} required/>
                </div>
                <div>
                    <button id={styles.submitBtn} type="submit">Create transaction!</button>
                </div>
            </form>
        </div>
    );
}
