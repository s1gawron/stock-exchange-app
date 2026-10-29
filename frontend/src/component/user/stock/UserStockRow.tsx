import React, {useState} from "react";
import {UserStockDTO} from "../../../dto/user/UserStockDTO.ts";
import styles from "./styles.module.css";
import ActionButton from "../../button/action/ActionButton.tsx";
import {formatQuantity} from "../../../util/QuantityFormatter.ts";

const COLUMN_COUNT: number = 11;

export default function UserStockRow({stock}: { stock: UserStockDTO }): React.ReactElement {
    const [positionsVisible, setPositionsVisible] = useState<boolean>(false);

    const priceChangeClass = stock.priceChange >= 0 ? styles.positiveChange : styles.negativeChange;
    const pctChangeClass = stock.percentagePriceChange >= 0 ? styles.positiveChange : styles.negativeChange;
    const profitLossClass = stock.profitLoss >= 0 ? styles.positiveChange : styles.negativeChange;

    return (
        <>
            <tr>
                <td className={`${styles.stockListingsCell} ${styles.stockListingsTd}`} data-label="Ticker">{stock.ticker}</td>
                <td className={`${styles.stockListingsCell} ${styles.stockListingsTd}`} data-label="Name">{stock.name}</td>
                <td className={`${styles.stockListingsCell} ${styles.stockListingsTd}`} data-label="Price">{stock.price.toFixed(4)} {stock.stockCurrency}</td>
                <td className={`${styles.stockListingsCell} ${styles.stockListingsTd} ${priceChangeClass}`}
                    data-label="Price change">{stock.priceChange.toFixed(4)}</td>
                <td className={`${styles.stockListingsCell} ${styles.stockListingsTd} ${pctChangeClass}`}
                    data-label="% change">{stock.percentagePriceChange.toFixed(2)}%
                </td>
                <td className={`${styles.stockListingsCell} ${styles.stockListingsTd}`} data-label="Quantity">{formatQuantity(stock.quantity)}</td>
                <td className={`${styles.stockListingsCell} ${styles.stockListingsTd}`} data-label="Qty blocked">{formatQuantity(stock.quantityBlocked)}</td>
                <td className={`${styles.stockListingsCell} ${styles.stockListingsTd}`}
                    data-label="Avg price">{stock.averagePurchasePrice.toFixed(4)} {stock.stockCurrency}</td>
                <td className={`${styles.stockListingsCell} ${styles.stockListingsTd} ${profitLossClass}`}
                    data-label="Profit/Loss">{stock.profitLoss.toFixed(4)} {stock.stockCurrency}</td>
                <td className={`${styles.stockListingsCell} ${styles.stockListingsTd}`} data-label="Positions">
                    <button className={styles.actionButton} onClick={() => setPositionsVisible(prev => !prev)} aria-expanded={positionsVisible}>
                        {positionsVisible ? "Hide" : "Show"} ({stock.positions.length})
                    </button>
                </td>
                <td className={`${styles.stockListingsCell} ${styles.stockListingsTd}`} data-label="Actions">
                    <ActionButton ticker={stock.ticker}/>
                </td>
            </tr>

            {positionsVisible && (
                <tr>
                    <td className={styles.stockListingsCell} colSpan={COLUMN_COUNT}>
                        <table className={styles.positionsTable}>
                            <thead>
                            <tr>
                                <th className={`${styles.stockListingsCell} ${styles.stockListingsTh}`}>Open date</th>
                                <th className={`${styles.stockListingsCell} ${styles.stockListingsTh}`}>Quantity</th>
                                <th className={`${styles.stockListingsCell} ${styles.stockListingsTh}`}>Quantity blocked</th>
                                <th className={`${styles.stockListingsCell} ${styles.stockListingsTh}`}>Open price</th>
                                <th className={`${styles.stockListingsCell} ${styles.stockListingsTh}`}>Profit/Loss</th>
                            </tr>
                            </thead>

                            <tbody>
                            {stock.positions.map(position => (
                                <tr key={position.id}>
                                    <td className={`${styles.stockListingsCell} ${styles.stockListingsTd}`}
                                        data-label="Open date">{new Date(position.openDate).toLocaleString()}</td>
                                    <td className={`${styles.stockListingsCell} ${styles.stockListingsTd}`}
                                        data-label="Quantity">{formatQuantity(position.quantity)}</td>
                                    <td className={`${styles.stockListingsCell} ${styles.stockListingsTd}`}
                                        data-label="Qty blocked">{formatQuantity(position.quantityBlocked)}</td>
                                    <td className={`${styles.stockListingsCell} ${styles.stockListingsTd}`}
                                        data-label="Open price">{position.openPrice.toFixed(4)} {stock.stockCurrency}</td>
                                    <td className={`${styles.stockListingsCell} ${styles.stockListingsTd} ${position.profitLoss >= 0 ? styles.positiveChange : styles.negativeChange}`}
                                        data-label="Profit/Loss">{position.profitLoss.toFixed(4)} {stock.stockCurrency}</td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    </td>
                </tr>
            )}
        </>
    );
}
