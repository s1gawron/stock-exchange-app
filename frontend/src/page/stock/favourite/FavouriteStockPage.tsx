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
import {useToast} from "../../../component/toast/ToastContext.ts";

export default function FavouriteStockPage(): React.ReactElement {
    const navigate = useNavigate();
    const [stocks, setStocks] = useState<FavouriteStockRowDTO[]>([]);
    const [loading, setLoading] = useState<boolean>(true);
    const {showError} = useToast();

    useEffect(() => {
        if (isUserNotAuthenticated()) {
            navigate("/user/login");
            return;
        }

        getUserFavouriteStocks().then(async res => {
            if (!res.success) {
                showError(res.errorMsg!);
                setLoading(false);
                return;
            }

            const favourites = res.responseBody!;
            const results = await Promise.allSettled(
                favourites.map(fav => getStockData(fav.ticker))
            );

            // A favourite without stock data is still listed, so the user can see and remove it
            const rows: FavouriteStockRowDTO[] = favourites.map((fav, index) => {
                const result = results[index];

                if (result.status === "fulfilled" && result.value.success) {
                    const data = result.value.responseBody!;
                    return {
                        ticker: fav.ticker,
                        stockData: {
                            companyFullName: data.companyFullName,
                            currentPrice: data.stockQuote.currentPrice,
                            priceChange: data.stockQuote.priceChange,
                            percentagePriceChange: data.stockQuote.percentagePriceChange,
                            stockCurrency: data.stockQuote.stockCurrency,
                        }
                    };
                }

                return {ticker: fav.ticker};
            });

            const failedTickers = rows.filter(row => row.stockData === undefined).map(row => row.ticker);

            if (failedTickers.length > 0) {
                showError("Could not load stock data for: " + failedTickers.join(", "));
            }

            setStocks(rows);
            setLoading(false);
        }).catch((error) => {
            console.error("An unexpected error occurred while loading favourite stocks:", error);
            setLoading(false);
        });
    }, [navigate, showError]);

    const handleRemove = (ticker: string) => {
        removeFavouriteStock(ticker).then(res => {
            if (res.success) {
                setStocks(prev => prev.filter(s => s.ticker !== ticker));
            } else {
                showError(res.errorMsg!);
            }
        }).catch((error) => {
            console.error("An unexpected error occurred while removing favourite stock:", error);
        });
    };

    return (
        <>
            <Topbar/>

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
