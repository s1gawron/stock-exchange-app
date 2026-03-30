import React from "react";
import styles from "./styles.module.css";
import {Link} from "react-router-dom";

export default function ActionButton({ticker}: { ticker: string }): React.ReactElement {
    return (

        <div className={styles.buttonWrapper}>
            <Link to={`/transaction/${ticker}`}>
                <button className={styles.actionButton}>Buy/Sell</button>
            </Link>
        </div>
    );
}