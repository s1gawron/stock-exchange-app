import React, {useEffect, useState} from 'react';
import Topbar from "../../../component/topbar/Topbar.tsx";
import styles from "./styles.module.css";
import Footer from "../../../component/footer/Footer.tsx";
import {FavouriteStockRowDTO} from "../../../dto/stock/FavouriteStockRowDTO.ts";
import FavouriteStocksData from "../../../component/stock/favourites/FavouriteStocksData.tsx";
import {useNavigate} from "react-router-dom";
import {isUserNotAuthenticated} from "../../../util/AuthUtil.ts";
import {getUserFavouriteStocks, removeFavouriteStock} from "../../../util/favouritestock/FavouriteStockService.ts";
import {getStockData} from "../../../util/stocklistings/StockService.ts";
import ErrorMsg from "../../../component/error/ErrorMsg.tsx";

export default function FavouriteStockPage(): React.ReactElement {
    const navigate = useNavigate();
    const [stocks, setStocks] = useState<FavouriteStockRowDTO[]>([]);
    const [loading, setLoading] = useState<boolean>(true);
    const [errMsg, setErrMsg] = useState<string>("");

    useEffect(() => {
        if (isUserNotAuthenticated()) {
            navigate("/user/login");
            return;
        }

        getUserFavouriteStocks().then(async res => {
            if (!res.success) {
                setErrMsg(res.errorMsg!);
                setLoading(false);
                return;
            }

            const favourites = res.responseBody!;
            const results = await Promise.allSettled(
                favourites.map(fav => getStockData(fav.ticker))
            );

            const rows: FavouriteStockRowDTO[] = [];
            results.forEach((result, index) => {
                if (result.status === "fulfilled" && result.value.success) {
                    const data = result.value.responseBody!;
                    rows.push({
                        ticker: data.ticker,
                        companyFullName: data.companyFullName,
                        currentPrice: data.stockQuote.currentPrice,
                        priceChange: data.stockQuote.priceChange,
                        percentagePriceChange: data.stockQuote.percentagePriceChange,
                        stockCurrency: data.stockQuote.stockCurrency,
                    });
                } else {
                    console.warn("Failed to load stock data for:", favourites[index].ticker);
                }
            });

            setStocks(rows);
            setLoading(false);
        }).catch((error) => {
            console.error("An unexpected error occurred while loading favourite stocks:", error);
            setLoading(false);
        });
    }, [navigate]);

    const handleRemove = (ticker: string) => {
        removeFavouriteStock(ticker).then(res => {
            if (res.success) {
                setStocks(prev => prev.filter(s => s.ticker !== ticker));
            } else {
                setErrMsg(res.errorMsg!);
            }
        }).catch((error) => {
            console.error("An unexpected error occurred while removing favourite stock:", error);
        });
    };

    return (
        <>
            <Topbar/>

            <div id={styles.errWrapper}>
                <ErrorMsg errMsg={errMsg}/>
            </div>

            <div id={styles.container}>
                {loading ? (
                    <div className={styles.loaderWrapper}>
                        <div className={styles.loader}></div>
                    </div>
                ) : <FavouriteStocksData stocks={stocks} onRemove={handleRemove}/>}
            </div>

            <Footer text="Uncover potential. Master strategies. Build your dream portfolio."/>
        </>
    );
}
