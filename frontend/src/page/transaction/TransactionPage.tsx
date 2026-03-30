import React, {useEffect, useState} from "react";
import Topbar from "../../component/topbar/Topbar.tsx";
import {isUserNotAuthenticated} from "../../util/AuthUtil.ts";
import TransactionForm from "../../component/form/transaction/TransactionForm.tsx";
import styles from "./styles.module.css";
import Footer from "../../component/footer/Footer.tsx";
import {useParams, useNavigate} from "react-router-dom";
import StockDataDTO from "../../dto/stock/StockDataDTO.ts";
import {getStockData} from "../../util/stocklistings/StockService.ts";
import StockDetails from "../../component/stock/details/StockDetails.tsx";
import ErrorMsg from "../../component/error/ErrorMsg.tsx";

const REDIRECT_TO_LOGIN_PAGE: string = "/user/login";

export default function TransactionPage(): React.ReactElement {
    const {ticker} = useParams<string>();
    const [stockData, setStockData] = useState<StockDataDTO>();
    const [errMsg, setErrMsg] = useState<string>("");
    const navigate = useNavigate();

    useEffect(() => {
        if (isUserNotAuthenticated()) {
            navigate(REDIRECT_TO_LOGIN_PAGE);
            return;
        }

        getStockData(ticker).then(res => {
            if (res.success) {
                setStockData(res.responseBody!);
                return;
            }

            setErrMsg(res.errorMsg!);
        }).catch((error) => {
            console.error("An unexpected error occurred while getting stock data:", error);
        });
    }, [navigate, ticker]);

    return (
        <>
            <Topbar/>

            <div id={styles.errWrapper}>
                <ErrorMsg errMsg={errMsg}/>
            </div>

            <div id={styles.stockDetailsWrapper}>
                <StockDetails stockData={stockData!}/>
            </div>

            <div>
                <TransactionForm ticker={ticker!}/>
            </div>

            <Footer text="Indeed, a wise choice!"/>
        </>
    );
}
