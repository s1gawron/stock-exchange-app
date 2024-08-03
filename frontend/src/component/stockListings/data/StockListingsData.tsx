import React from 'react';
import {Link} from "react-router-dom";
import styles from "./styles.module.css";
import {KEYS, StockListingsDTO} from "../../../dto/stock/StockListingsDTO";

export default function StockListingsData({index, stockListings}: { index: string | undefined, stockListings: StockListingsDTO }): React.ReactElement {
    const keys = KEYS.map(key =>
        <li key={key} className={styles.stockListingKeyLI}>
            <Link to={`/stockListings/${index}?q=${key}`}>
                <button className={`userLinkBtn ${styles.stockListingsIndex}`}>{key}</button>
            </Link>
        </li>
    );

    return (
        <div>
            {stockListings.count !== 0 ? (<div className={styles.loader}></div>) :
                (
                    <div>
                        <div id={styles.stockListingKeyListWrapper}>
                            <ol id={styles.stockListingKeyList}>
                                {keys}
                            </ol>
                        </div>

                        <table>
                            <tr id="sign">
                                <th>Ticker</th>
                                <th>Name</th>
                                <th>Industry</th>
                                <th>Details</th>
                            </tr>

                            <tr>
                                <td>ticker</td>
                                <td>name</td>
                                <td>industry</td>
                                <td>
                                    <div className="buttonWrapper">
                                        <Link to="/transaction/$index/$ticker">
                                            <button className="userLinkBtn">Details</button>
                                        </Link>
                                    </div>
                                </td>
                            </tr>
                        </table>
                    </div>
                )
            }
        </div>
    );
}
