import StockDataDTO from "../../../dto/stock/StockDataDTO.ts";
import styles from "./styles.module.css";

export default function StockDetails({stockData}: { stockData: StockDataDTO }) {
    return (
        <div id={styles.stockInfoWrapper}>
            <fieldset id={styles.stockInfo}>
                <legend> Stock info:</legend>
                <div id={styles.stockInfoLegend} className={styles.stockInfoFont}>
                    <p>Ticker:</p>
                    <p>Company full name:</p>
                    <p>Industry:</p>
                    <p>Company origin country:</p>
                    <p>Stock exchange:</p>
                    <p>Market capitalization:</p>
                    <br/>
                    <p>Current price:</p>
                    <p>Previous price:</p>
                    <p>Price change:</p>
                    <p>Percentage price change:</p>
                    <p>Update date:</p>
                </div>

                <div id={styles.stockData} className={styles.stockInfoFont}>
                    <p>{stockData === undefined ? '-' : stockData?.ticker}</p>
                    <p>{stockData === undefined ? '-' : stockData?.companyFullName}</p>
                    <p>{stockData === undefined ? '-' : stockData?.companyIndustry}</p>
                    <p>{stockData === undefined ? '-' : stockData?.companyOriginCountry}</p>
                    <p>{stockData === undefined ? '-' : stockData?.stockExchange}</p>
                    <p>{stockData === undefined ? '-' : stockData?.marketCapitalization.toFixed(2) + " USD"}</p>
                    <br/>
                    <p>{stockData === undefined ? '-' : stockData?.stockQuote.currentPrice.toFixed(2) + " USD"}</p>
                    <p>{stockData === undefined ? '-' : stockData?.stockQuote.previousClosePrice.toFixed(2) + " USD"}</p>
                    <p>{stockData === undefined ? '-' : stockData?.stockQuote.priceChange.toFixed(2) + " USD"}</p>
                    <p>{stockData === undefined ? '-' : stockData?.stockQuote.percentagePriceChange.toFixed(2) + '%'}</p>
                    <p>{stockData === undefined ? '-' : new Date(stockData?.lastUpdateDate).toLocaleString()}</p>
                </div>
            </fieldset>
        </div>
    );
}