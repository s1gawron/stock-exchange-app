import React, {useEffect, useState} from 'react';
import Topbar from "../../component/topbar/Topbar";
import Menubar from "../../component/menubar/Menubar";
import styles from "./styles.module.css";
import Footer from "../../component/footer/Footer";
import {generateInitialStockListingsState, StockListingsDTO} from "../../dto/stock/StockListingsDTO";
import StockListingsData from "../../component/stockListings/data/StockListingsData";
import StockListingsHeader from "../../component/stockListings/header/StockListingsHeader";
import {useParams} from "react-router-dom";
import {getIndexStockListings} from "../../util/stocklistings/StockService";
import ErrorMsg from "../../component/error/ErrorMsg";

export default function StockListingsPage(): React.ReactElement {
    const {index} = useParams<string>();
    const [stockListings, setStockListings] = useState<StockListingsDTO>(generateInitialStockListingsState);
    const [errMsg, setErrMsg] = useState<string>("");

    const loadStockListings = (indexArg: string | undefined) => {
        getIndexStockListings(indexArg).then(res => {
            if (res.success) {
                setStockListings(res.responseBody!);
                setErrMsg("");
                return;
            }

            setErrMsg(res.errorMsg!);
        }).catch((error) => {
            console.error("An unexpected error occurred while loading user wallet details:", error);
        });
    }

    useEffect(() => loadStockListings(index), [index]);

    return (
        <>
            <Topbar/>
            <Menubar/>

            <div id={styles.errWrapper}>
                <ErrorMsg errMsg={errMsg}/>
            </div>

            <div id={styles.container}>
                <StockListingsHeader onHeaderChange={loadStockListings}/>
                {stockListings.count === 0 ? (<div className={styles.loader}></div>) : <StockListingsData index={index} stockListings={stockListings}/>}
            </div>

            <Footer text="Uncover potential. Master strategies. Build your dream portfolio."/>
        </>
    );
}
