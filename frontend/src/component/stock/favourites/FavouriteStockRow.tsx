import React from "react";
import styles from "./styles.module.css";
import {FavouriteStockRowDTO} from "../../../dto/stock/FavouriteStockRowDTO.ts";
import ActionButton from "../../button/action/ActionButton.tsx";

export default function FavouriteStockRow({stock, onRemove}: {
    stock: FavouriteStockRowDTO,
    onRemove: (ticker: string) => void
}): React.ReactElement {
    const priceChangeClass = stock.priceChange >= 0 ? styles.positiveChange : styles.negativeChange;
    const pctChangeClass = stock.percentagePriceChange >= 0 ? styles.positiveChange : styles.negativeChange;

    return (
        <tr>
            <td className={`${styles.stockListingsCell} ${styles.stockListingsTd}`} data-label="Ticker">{stock.ticker}</td>
            <td className={`${styles.stockListingsCell} ${styles.stockListingsTd}`} data-label="Name">{stock.companyFullName}</td>
            <td className={`${styles.stockListingsCell} ${styles.stockListingsTd}`} data-label="Current Price">{stock.currentPrice} {stock.stockCurrency}</td>
            <td className={`${styles.stockListingsCell} ${styles.stockListingsTd} ${priceChangeClass}`} data-label="Price Change">{stock.priceChange}</td>
            <td className={`${styles.stockListingsCell} ${styles.stockListingsTd} ${pctChangeClass}`} data-label="% Change">{stock.percentagePriceChange}%</td>
            <td className={`${styles.stockListingsCell} ${styles.stockListingsTd}`} data-label="Actions">
                <div className={styles.buttonWrapper}>
                    <ActionButton ticker={stock.ticker}/>
                </div>
            </td>
            <td className={`${styles.stockListingsCell} ${styles.stockListingsTd}`} data-label="Favourites">
                <div className={styles.buttonWrapper}>
                    <button className={styles.favouriteButtonActive} onClick={() => onRemove(stock.ticker)}>★ Remove</button>
                </div>
            </td>
        </tr>
    );
}
