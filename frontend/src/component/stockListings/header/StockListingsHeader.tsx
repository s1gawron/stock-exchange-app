import React, {useEffect} from "react";
import styles from "./styles.module.css";
import {Link} from "react-router-dom";
import {getIndexStockListings} from "../../../util/StockListingsRestService";
import {StockListingsDTO} from "../../../dto/stock/StockListingsDTO";

export default function StockListingsHeader({index, queryParam, setStockListings}: {
    index: string | undefined,
    queryParam: string | null,
    setStockListings: React.Dispatch<React.SetStateAction<StockListingsDTO>>
}): React.ReactElement {
    useEffect(() => getIndexStockListings(index, queryParam, setStockListings), [index, queryParam, setStockListings]);

    const handleIndexChange = (index: string) => {
        getIndexStockListings(index, queryParam, setStockListings);
    }

    return (
        <div>
            <div id={styles.menu}>
                <ol id={styles.stockListingsList}>
                    <li className={styles.stockListingsLI}>
                        <Link to="/stockListings/DJI" onClick={() => handleIndexChange("DJI")}>
                            <button className={`userLinkBtn ${styles.stockListingsIndex}`}>DJI</button>
                        </Link>
                    </li>
                    <li className={styles.stockListingsLI}>
                        <Link to="/stockListings/NASDAQ100" onClick={() => handleIndexChange("NASDAQ100")}>
                            <button className={`userLinkBtn ${styles.stockListingsIndex}`}>NASDAQ 100</button>
                        </Link>
                    </li>
                    <li className={styles.stockListingsLI}>
                        <Link to="/stockListings/SP500" onClick={() => handleIndexChange("SP500")}>
                            <button className={`userLinkBtn ${styles.stockListingsIndex}`}>S&P 500</button>
                        </Link>
                    </li>
                </ol>
            </div>
        </div>
    );
}
