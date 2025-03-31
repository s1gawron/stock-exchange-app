import {UserStockDTO} from "../../dto/user/UserStockDTO.ts";
import React from "react";
import UserStockRow from "./UserStockRow.tsx";
import styles from "./styles.module.css";

export default function UserStockData({userStocks}: { userStocks: UserStockDTO[] }): React.ReactElement {
    const userStocksLI = userStocks?.map(stock => (
        <UserStockRow key={stock.ticker} stock={stock}/>
    )) ?? [];

    return (
        <>
            {userStocksLI.length === 0 ? (<div id={styles.portfolioEmpty}>Your portfolio is empty.</div>) : (
                <fieldset id="stock">
                    <legend>Stocks in your wallet:</legend>
                    <table id={styles.stockListingsTable}>
                        <thead>
                        <tr>
                            <th className={`${styles.stockListingsCell} ${styles.stockListingsTh}`}>Ticker</th>
                            <th className={`${styles.stockListingsCell} ${styles.stockListingsTh}`}>Name</th>
                            <th className={`${styles.stockListingsCell} ${styles.stockListingsTh}`}>Price</th>
                            <th className={`${styles.stockListingsCell} ${styles.stockListingsTh}`}>Price change</th>
                            <th className={`${styles.stockListingsCell} ${styles.stockListingsTh}`}>Percentage price change</th>
                            <th className={`${styles.stockListingsCell} ${styles.stockListingsTh}`}>Quantity</th>
                            <th className={`${styles.stockListingsCell} ${styles.stockListingsTh}`}>Quantity blocked</th>
                            <th className={`${styles.stockListingsCell} ${styles.stockListingsTh}`}>Average purchase price</th>
                            <th className={`${styles.stockListingsCell} ${styles.stockListingsTh}`}>Profit/Loss</th>
                            <th className={`${styles.stockListingsCell} ${styles.stockListingsTh}`}>Actions</th>
                        </tr>
                        </thead>

                        <tbody>
                        {userStocksLI}
                        </tbody>
                    </table>
                </fieldset>
            )}
        </>
    );
}