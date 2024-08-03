import React, {useEffect, useState} from 'react';
import Topbar from "../../component/topbar/Topbar";
import Menubar from "../../component/menubar/Menubar";
import styles from "./styles.module.css";
import Footer from "../../component/footer/Footer";
import {generateInitialStockListingsState, StockListingsDTO} from "../../dto/stock/StockListingsDTO";
import StockListingsData from "../../component/stockListings/data/StockListingsData";
import StockListingsHeader from "../../component/stockListings/header/StockListingsHeader";
import {useParams, useSearchParams} from "react-router-dom";
import {getIndexStockListings} from "../../util/StockListingsRestService";

export default function StockListingsPage(): React.ReactElement {
    const {index} = useParams<string>();
    const [searchParams] = useSearchParams();
    const [stockListings, setStockListings] = useState<StockListingsDTO>(generateInitialStockListingsState);
    const queryParam = searchParams.get('q');

    useEffect(() => getIndexStockListings(index, setStockListings), [index, setStockListings]);

    return (
        <div>
            <Topbar/>
            <Menubar/>

            <div id={styles.container}>
                <StockListingsHeader index={index} setStockListings={setStockListings}/>
                {stockListings.count === 0 ? (<div className={styles.loader}></div>) : <StockListingsData index={index} stockListings={stockListings}/>}
            </div>

            <Footer text="To change - Thank you for your visit! I hope, that purchases will be successful :)"/>
        </div>
    );
}
