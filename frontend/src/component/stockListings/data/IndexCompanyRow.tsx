import {IndexCompanyDTO} from "../../../dto/stock/StockListingsDTO.ts";
import styles from "./styles.module.css";
import {Link} from "react-router-dom";
import React from "react";

export default function IndexCompanyRow({company}: { company: IndexCompanyDTO }): React.ReactElement {
    return (
        <tr key={company.ticker}>
            <td className={`${styles.stockListingsCell} ${styles.stockListingsTd} ${styles.oddTableData}`}>{company.ticker}</td>
            <td className={`${styles.stockListingsCell} ${styles.stockListingsTd}`}>{company.name}</td>
            <td className={`${styles.stockListingsCell} ${styles.stockListingsTd} ${styles.oddTableData}`}>{company.industry}</td>
            <td className={`${styles.stockListingsCell} ${styles.stockListingsTd}`}>
                <div className={styles.buttonWrapper}>
                    <Link to={`/transaction/${company.ticker}`}>
                        <button className={styles.actionButton}>Buy/Sell</button>
                    </Link>
                </div>
            </td>
        </tr>
    );
}
