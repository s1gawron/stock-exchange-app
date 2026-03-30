import React from "react";
import styles from "./styles.module.css";
import {StockSearchResultDTO} from "../../../dto/stock/StockSearchDTO.ts";
import ActionButton from "../../button/action/ActionButton.tsx";

export default function StockSearchRow({result, userAuthenticated, isFavourite, isPending, onToggleFavourite}: {
    result: StockSearchResultDTO,
    userAuthenticated: boolean,
    isFavourite: boolean,
    isPending: boolean,
    onToggleFavourite: (ticker: string) => void
}): React.ReactElement {
    return (
        <tr>
            <td className={`${styles.stockSearchCell} ${styles.stockSearchTd}`} data-label="Symbol">{result.displaySymbol}</td>
            <td className={`${styles.stockSearchCell} ${styles.stockSearchTd}`} data-label="Description">{result.description}</td>
            <td className={`${styles.stockSearchCell} ${styles.stockSearchTd}`} data-label="Type">{result.type}</td>
            <td className={`${styles.stockSearchCell} ${styles.stockSearchTd}`} data-label="Actions">
                <div className={styles.buttonWrapper}>
                    <ActionButton ticker={result.symbol}/>
                </div>
            </td>
            {userAuthenticated && (
                <td className={`${styles.stockSearchCell} ${styles.stockSearchTd}`} data-label="Favourite">
                    <div className={styles.buttonWrapper}>
                        <button
                            className={isFavourite ? styles.favouriteButtonActive : styles.favouriteButton}
                            onClick={() => onToggleFavourite(result.symbol)}
                            disabled={isPending}
                        >
                            {isFavourite ? "★ Remove" : "☆ Add"}
                        </button>
                    </div>
                </td>
            )}
        </tr>
    );
}
