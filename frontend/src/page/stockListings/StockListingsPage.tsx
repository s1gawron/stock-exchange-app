import React, {useEffect, useState} from 'react';
import Topbar from "../../component/topbar/Topbar";
import Menubar from "../../component/menubar/Menubar";
import styles from "./styles.module.css";
import Footer from "../../component/footer/Footer";
import {generateInitialStockListingsState, StockListingsDTO} from "../../dto/stock/StockListingsDTO";
import StockListingsData from "../../component/stockListings/data/StockListingsData";
import StockListingsHeader from "../../component/stockListings/header/StockListingsHeader";
import {useParams} from "react-router-dom";
import {getIndexStockListings} from "../../util/StockListingsRestService";

export default function StockListingsPage(): React.ReactElement {
    const {index} = useParams<string>();
    const [stockListings, setStockListings] = useState<StockListingsDTO>(generateInitialStockListingsState);

    useEffect(() => getIndexStockListings(index, setStockListings), [index, setStockListings]);

    return (
        <div>
            <Topbar/>
            <Menubar/>

            <div id={styles.container}>
                <StockListingsHeader setStockListings={setStockListings}/>
                {stockListings.count === 0 ? (<div className={styles.loader}></div>) : <StockListingsData index={index} stockListings={stockListings}/>}
            </div>

            <Footer text="Uncover potential. Master strategies. Build your dream portfolio."/>
        </div>
    );
}
