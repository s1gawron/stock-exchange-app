import React from "react";
import styles from "./styles.module.css";
import {FavouriteStockRowDTO} from "../../../dto/stock/FavouriteStockRowDTO.ts";
import ActionButton from "../../button/action/ActionButton.tsx";

const NO_DATA: string = "-";

function changeClass(change: number | undefined): string {
    if (change === undefined) {
        return "";
    }

    return change >= 0 ? styles.positiveChange : styles.negativeChange;
}

export default function FavouriteStockRow({stock, onRemove}: {
    stock: FavouriteStockRowDTO,
    onRemove: (ticker: string) => void
}): React.ReactElement {
    const data = stock.stockData;

    return (
        <tr>
            <td className={`${styles.stockListingsCell} ${styles.stockListingsTd}`} data-label="Ticker">{stock.ticker}</td>
            <td className={`${styles.stockListingsCell} ${styles.stockListingsTd}`} data-label="Name">
                {data === undefined ? <span className={styles.dataUnavailable}>Data unavailable</span> : data.companyFullName}
            </td>
            <td className={`${styles.stockListingsCell} ${styles.stockListingsTd}`} data-label="Current Price">
                {data === undefined ? NO_DATA : `${data.currentPrice.toFixed(4)} ${data.stockCurrency}`}
            </td>
            <td className={`${styles.stockListingsCell} ${styles.stockListingsTd} ${changeClass(data?.priceChange)}`} data-label="Price Change">
                {data === undefined ? NO_DATA : data.priceChange.toFixed(4)}
            </td>
            <td className={`${styles.stockListingsCell} ${styles.stockListingsTd} ${changeClass(data?.percentagePriceChange)}`} data-label="% Change">
                {data === undefined ? NO_DATA : `${data.percentagePriceChange.toFixed(2)}%`}
            </td>
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
