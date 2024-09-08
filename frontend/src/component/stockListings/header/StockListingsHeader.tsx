import React from "react";
import styles from "./styles.module.css";
import {getIndexStockListings} from "../../../util/stocklistings/StockListingsService";
import {StockListingsDTO} from "../../../dto/stock/StockListingsDTO";
import LinkButton from "../../linkButton/LinkButton";

export default function StockListingsHeader({setStockListings}: {
    setStockListings: React.Dispatch<React.SetStateAction<StockListingsDTO>>
}): React.ReactElement {
    const handleIndexChange = (index: string) => {
        getIndexStockListings(index, setStockListings);
    }

    const listItems: React.JSX.Element[] = ["DJI", "NASDAQ100", "SP500"].map(key => {
        return (
            <div className={styles.stockListingsLIWrapper}>
                <li key={key} className={styles.stockListingsLI}>
                    <LinkButton props={{linkTo: `/stockListings/${key}`, text: key, onBtnClick: () => handleIndexChange(key)}}/>
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
