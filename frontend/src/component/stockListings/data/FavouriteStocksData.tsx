import React from 'react';
import {Link} from "react-router-dom";
import styles from "./styles.module.css";
import {FavouriteStockRowDTO} from "../../../dto/stock/FavouriteStockRowDTO.ts";
import FavouriteStockRow from "./FavouriteStockRow.tsx";

export default function FavouriteStocksData({stocks, onRemove}: {
    stocks: FavouriteStockRowDTO[],
    onRemove: (ticker: string) => void
}): React.ReactElement {
    if (stocks.length === 0) {
        return (
            <div id={styles.noResults}>
                <p>You have no favourite stocks yet. Use the search page to find stocks.</p>
                <Link to="/stockSearch">
                    <button id={styles.searchBtn}>Search page</button>
                </Link>
            </div>
        );
    }

    return (
        <div className={styles.tableWrapper}>
            <table id={styles.stockListingsTable}>
                <thead>
                <tr>
                    <th className={`${styles.stockListingsCell} ${styles.stockListingsTh}`}>Ticker</th>
                    <th className={`${styles.stockListingsCell} ${styles.stockListingsTh}`}>Name</th>
                    <th className={`${styles.stockListingsCell} ${styles.stockListingsTh}`}>Current Price</th>
                    <th className={`${styles.stockListingsCell} ${styles.stockListingsTh}`}>Price Change</th>
                    <th className={`${styles.stockListingsCell} ${styles.stockListingsTh}`}>% Change</th>
                    <th className={`${styles.stockListingsCell} ${styles.stockListingsTh}`}>Actions</th>
                    <th className={`${styles.stockListingsCell} ${styles.stockListingsTh}`}>Favourites</th>
                </tr>
                </thead>

                <tbody>
                {stocks.map(stock => (
                    <FavouriteStockRow key={stock.ticker} stock={stock} onRemove={onRemove}/>
                ))}
                </tbody>
            </table>
        </div>
    );
}
