import React from "react";
import styles from "./styles.module.css";
import LinkButton from "../../linkButton/LinkButton.tsx";

export default function StockListingsHeader({onHeaderChange}: {
    onHeaderChange: (index: string | undefined) => void
}): React.ReactElement {

    const listItems = ["DJI", "NASDAQ100", "SP500"].map(key => {
        return (
            <li key={key} className={styles.stockListingsLI}>
                <div className={styles.stockListingsLIWrapper}>
                    <LinkButton props={{linkTo: `/stockListings/${key}`, text: key, onBtnClick: () => onHeaderChange(key)}}/>
                </div>
            </li>
        );
    })

    return (
        <div id={styles.menu}>
            <ol id={styles.stockListingsList}>
                <div id={styles.listItemsContainer}>
                    {listItems}
                    <div style={{clear: "both"}}></div>
                </div>
            </ol>
        </div>
    );
}
