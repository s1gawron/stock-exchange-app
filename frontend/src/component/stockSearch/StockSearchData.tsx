import React from "react";
import styles from "./styles.module.css";
import {StockSearchResultDTO} from "../../dto/stock/StockSearchDTO.ts";
import StockSearchRow from "./StockSearchRow.tsx";

export default function StockSearchData({results, query, loading, userAuthenticated, favouriteTickers, pendingTickers, onToggleFavourite}: {
    results: StockSearchResultDTO[],
    query: string,
    loading: boolean,
    userAuthenticated: boolean,
    favouriteTickers: Set<string>,
    pendingTickers: Set<string>,
    onToggleFavourite: (ticker: string) => void
}): React.ReactElement | null {
    if (loading) {
        return (
            <div className={styles.loaderWrapper}>
                <div className={styles.loader}></div>
            </div>
        );
    }

    if (results.length === 0 && query.length >= 2) {
        return <div id={styles.noResults}>No results found for "{query}"</div>;
    }

    if (results.length === 0) {
        return null;
    }

    return (
        <div className={styles.tableWrapper}>
            <table id={styles.stockSearchTable}>
                <thead>
                <tr>
                    <th className={`${styles.stockSearchCell} ${styles.stockSearchTh}`}>Symbol</th>
                    <th className={`${styles.stockSearchCell} ${styles.stockSearchTh}`}>Description</th>
                    <th className={`${styles.stockSearchCell} ${styles.stockSearchTh}`}>Type</th>
                    <th className={`${styles.stockSearchCell} ${styles.stockSearchTh}`}>Actions</th>
                    {userAuthenticated && <th className={`${styles.stockSearchCell} ${styles.stockSearchTh}`}>Favourite</th>}
                </tr>
                </thead>
                <tbody>
                {results.map((result, index) => (
                    <StockSearchRow
                        key={result.symbol + index}
                        result={result}
                        userAuthenticated={userAuthenticated}
                        isFavourite={favouriteTickers.has(result.symbol)}
                        isPending={pendingTickers.has(result.symbol)}
                        onToggleFavourite={onToggleFavourite}
                    />
                ))}
                </tbody>
            </table>
        </div>
    );
}
