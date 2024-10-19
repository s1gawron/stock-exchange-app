import React, {useEffect, useState} from "react";
import Topbar from "../../component/topbar/Topbar";
import Menubar from "../../component/menubar/Menubar";
import AuthUtil from "../../util/AuthUtil";
import RedirectUtil from "../../util/RedirectUtil";
import AbstractForm from "../../component/form/AbstractForm";
import styles from "./styles.module.css";
import SplitView from "../../component/splitView/SplitView";
import Footer from "../../component/footer/Footer";
import {useParams} from "react-router-dom";
import StockDataDTO from "../../dto/stock/StockDataDTO";
import {getStockData} from "../../util/stocklistings/StockService";
import ErrorMsg from "../../component/error/ErrorMsg";
import {TransactionRequestDTO, TransactionType} from "../../dto/transaction/TransactionRequestDTO";
import {createTransaction} from "../../util/transaction/TransactionService";

const REDIRECT_TO_LOGIN_PAGE = "/user/login";
const REDIRECT_URL_AFTER_CREATE_TRANSACTION_SUCCESS = "/user/wallet";

export default function TransactionPage(): React.ReactElement {
    const {ticker} = useParams<string>();
    const initialValues: TransactionRequestDTO = {
        type: TransactionType.PURCHASE,
        stockTicker: ticker!,
        price: 0.00,
        quantity: 0
    };

    const [stockData, setStockData] = useState<StockDataDTO>();
    const [errMsg, setErrMsg] = useState<string>("");

    useEffect(() => {
        if (AuthUtil.isUserNotAuthenticated()) {
            RedirectUtil.redirectTo(REDIRECT_TO_LOGIN_PAGE);
            return;
        }

        getStockData(ticker).then(res => {
            if (res.success) {
                setStockData(res.responseBody!);
                setErrMsg("");
                return;
            }

            setErrMsg(res.errorMsg!);
        }).catch((error) => {
            console.error("An unexpected error occurred while loading user wallet details:", error);
        });
    }, [ticker]);

    const left: React.ReactElement =
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

                <div style={{clear: "both"}}></div>
            </fieldset>
        </div>

    const handleSubmit = (values: TransactionRequestDTO) => {
        console.log(values);
        createTransaction(values).then(res => {
            if (res.success) {
                RedirectUtil.redirectTo(REDIRECT_URL_AFTER_CREATE_TRANSACTION_SUCCESS);
                setErrMsg("");
                return;
            }

            setErrMsg(res.errorMsg!);
        }).catch((error) => {
            console.error("An unexpected error occurred while creating transaction:", error);
        });
    }

    const right: React.ReactElement =
        <div>
            <AbstractForm
                initialValues={initialValues}
                onSubmit={handleSubmit}
                fields={[
                    {
                        name: "type",
                        type: "radio",
                        label: "Transaction type",
                        options: [{value: TransactionType.PURCHASE, label: "Buy"}, {value: TransactionType.SELL, label: "Sell"}]
                    },
                    {name: "stockTicker", type: "text", value: ticker, hidden: true},
                    {name: "price", type: "number", label: "Price"},
                    {name: "quantity", type: "number", label: "Quantity"},
                ]}
                submitButtonText="Create transaction!"
            />
        </div>

    return (
        <>
            <Topbar/>
            <Menubar/>

            <div id={styles.errWrapper}>
                <ErrorMsg errMsg={errMsg}/>
            </div>

            <div id={styles.splitViewWrapper}>
                <SplitView left={left} right={right}/>
            </div>

            <Footer text="Indeed, a wise choice!"/>
        </>
    );
}
