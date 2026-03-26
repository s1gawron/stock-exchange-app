import React from "react";
import styles from "./styles.module.css";
import {Link} from "react-router-dom";
import {FavouriteStockRowDTO} from "../../../dto/stock/FavouriteStockRowDTO.ts";

export default function FavouriteStockRow({stock, onRemove}: {
    stock: FavouriteStockRowDTO,
    onRemove: (ticker: string) => void
}): React.ReactElement {
    const priceChangeClass = stock.priceChange >= 0 ? styles.positiveChange : styles.negativeChange;
    const pctChangeClass = stock.percentagePriceChange >= 0 ? styles.positiveChange : styles.negativeChange;

    return (
        <tr>
            <td className={`${styles.stockListingsCell} ${styles.stockListingsTd}`}>{stock.ticker}</td>
            <td className={`${styles.stockListingsCell} ${styles.stockListingsTd}`}>{stock.companyFullName}</td>
            <td className={`${styles.stockListingsCell} ${styles.stockListingsTd}`}>{stock.currentPrice} {stock.stockCurrency}</td>
            <td className={`${styles.stockListingsCell} ${styles.stockListingsTd} ${priceChangeClass}`}>{stock.priceChange}</td>
            <td className={`${styles.stockListingsCell} ${styles.stockListingsTd} ${pctChangeClass}`}>{stock.percentagePriceChange}%</td>
            <td className={`${styles.stockListingsCell} ${styles.stockListingsTd}`}>
                <div className={styles.buttonWrapper}>
                    <Link to={`/transaction/${stock.ticker}`}>
                        <button className={styles.actionButton}>Buy/Sell</button>
                    </Link>
                </div>
            </td>
            <td className={`${styles.stockListingsCell} ${styles.stockListingsTd}`}>
                <div className={styles.buttonWrapper}>
                    <button className={styles.favouriteButtonActive} onClick={() => onRemove(stock.ticker)}>★ Remove</button>
                </div>
            </td>
        </tr>
    );
}
