import React from "react";
import {UserStockDTO} from "../../dto/user/UserStockDTO.ts";
import styles from "./styles.module.css";
import {Link} from "react-router-dom";

export default function UserStockRow({stock}: { stock: UserStockDTO }): React.ReactElement {
    const priceChangeClass = stock.priceChange >= 0 ? styles.positiveChange : styles.negativeChange;
    const pctChangeClass = stock.percentagePriceChange >= 0 ? styles.positiveChange : styles.negativeChange;
    const profitLossClass = stock.profitLoss >= 0 ? styles.positiveChange : styles.negativeChange;

    return (
        <tr key={stock.ticker}>
            <td className={`${styles.stockListingsCell} ${styles.stockListingsTd}`}>{stock.ticker}</td>
            <td className={`${styles.stockListingsCell} ${styles.stockListingsTd}`}>{stock.name}</td>
            <td className={`${styles.stockListingsCell} ${styles.stockListingsTd}`}>{stock.price.toFixed(2)}</td>
            <td className={`${styles.stockListingsCell} ${styles.stockListingsTd} ${priceChangeClass}`}>{stock.priceChange.toFixed(2)}</td>
            <td className={`${styles.stockListingsCell} ${styles.stockListingsTd} ${pctChangeClass}`}>{stock.percentagePriceChange.toFixed(2)}%</td>
            <td className={`${styles.stockListingsCell} ${styles.stockListingsTd}`}>{stock.quantity}</td>
            <td className={`${styles.stockListingsCell} ${styles.stockListingsTd}`}>{stock.quantityBlocked}</td>
            <td className={`${styles.stockListingsCell} ${styles.stockListingsTd}`}>{stock.averagePurchasePrice.toFixed(2)}</td>
            <td className={`${styles.stockListingsCell} ${styles.stockListingsTd} ${profitLossClass}`}>{stock.profitLoss.toFixed(2)}</td>
            <td className={`${styles.stockListingsCell} ${styles.stockListingsTd}`}>
                <div className={styles.buttonWrapper}>
                    <Link to={`/transaction/${stock.ticker}`}>
                        <button className={styles.actionButton}>Buy/Sell</button>
                    </Link>
                </div>
            </td>
        </tr>
    );
}