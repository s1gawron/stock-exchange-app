import React from "react";
import styles from "./styles.module.css";
import LinkButton from "../../linkButton/LinkButton";

export default function StockListingsHeader({onHeaderChange}: {
    onHeaderChange: (index: string | undefined) => void
}): React.ReactElement {

    const listItems: React.JSX.Element[] = ["DJI", "NASDAQ100", "SP500"].map(key => {
        return (
            <div className={styles.stockListingsLIWrapper}>
                <li key={key} className={styles.stockListingsLI}>
                    <LinkButton props={{linkTo: `/stockListings/${key}`, text: key, onBtnClick: () => onHeaderChange(key)}}/>
                </li>
            </div>
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
