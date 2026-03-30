import React from "react";
import {UserStockDTO} from "../../../dto/user/UserStockDTO.ts";
import styles from "./styles.module.css";
import ActionButton from "../../button/action/ActionButton.tsx";

export default function UserStockRow({stock}: { stock: UserStockDTO }): React.ReactElement {
    const priceChangeClass = stock.priceChange >= 0 ? styles.positiveChange : styles.negativeChange;
    const pctChangeClass = stock.percentagePriceChange >= 0 ? styles.positiveChange : styles.negativeChange;
    const profitLossClass = stock.profitLoss >= 0 ? styles.positiveChange : styles.negativeChange;

    return (
        <tr key={stock.ticker}>
            <td className={`${styles.stockListingsCell} ${styles.stockListingsTd}`} data-label="Ticker">{stock.ticker}</td>
            <td className={`${styles.stockListingsCell} ${styles.stockListingsTd}`} data-label="Name">{stock.name}</td>
            <td className={`${styles.stockListingsCell} ${styles.stockListingsTd}`} data-label="Price">{stock.price.toFixed(2)}</td>
            <td className={`${styles.stockListingsCell} ${styles.stockListingsTd} ${priceChangeClass}`}
                data-label="Price change">{stock.priceChange.toFixed(2)}</td>
            <td className={`${styles.stockListingsCell} ${styles.stockListingsTd} ${pctChangeClass}`}
                data-label="% change">{stock.percentagePriceChange.toFixed(2)}%
            </td>
            <td className={`${styles.stockListingsCell} ${styles.stockListingsTd}`} data-label="Quantity">{stock.quantity}</td>
            <td className={`${styles.stockListingsCell} ${styles.stockListingsTd}`} data-label="Qty blocked">{stock.quantityBlocked}</td>
            <td className={`${styles.stockListingsCell} ${styles.stockListingsTd}`} data-label="Avg price">{stock.averagePurchasePrice.toFixed(2)}</td>
            <td className={`${styles.stockListingsCell} ${styles.stockListingsTd} ${profitLossClass}`}
                data-label="Profit/Loss">{stock.profitLoss.toFixed(2)}</td>
            <td className={`${styles.stockListingsCell} ${styles.stockListingsTd}`} data-label="Actions">
                <ActionButton ticker={stock.ticker}/>
            </td>
        </tr>
    );
}