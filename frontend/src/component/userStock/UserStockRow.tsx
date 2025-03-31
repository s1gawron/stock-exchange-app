import React from "react";
import {UserStockDTO} from "../../dto/user/UserStockDTO.ts";
import styles from "./styles.module.css";
import {Link} from "react-router-dom";

export default function UserStockRow({stock}: { stock: UserStockDTO }): React.ReactElement {
    return (
        <tr key={stock.ticker}>
            <td className={`${styles.stockListingsCell} ${styles.stockListingsTd} ${styles.oddTableData}`}>{stock.ticker}</td>
            <td className={`${styles.stockListingsCell} ${styles.stockListingsTd}`}>{stock.name}</td>
            <td className={`${styles.stockListingsCell} ${styles.stockListingsTd} ${styles.oddTableData}`}>{stock.price}</td>
            <td className={`${styles.stockListingsCell} ${styles.stockListingsTd}`}>{stock.priceChange}</td>
            <td className={`${styles.stockListingsCell} ${styles.stockListingsTd} ${styles.oddTableData}`}>{stock.percentagePriceChange}%</td>
            <td className={`${styles.stockListingsCell} ${styles.stockListingsTd}`}>{stock.quantity}</td>
            <td className={`${styles.stockListingsCell} ${styles.stockListingsTd} ${styles.oddTableData}`}>{stock.quantityBlocked}</td>
            <td className={`${styles.stockListingsCell} ${styles.stockListingsTd}`}>{stock.averagePurchasePrice}</td>
            <td className={`${styles.stockListingsCell} ${styles.stockListingsTd} ${styles.oddTableData}`}>{stock.profitLoss}</td>
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