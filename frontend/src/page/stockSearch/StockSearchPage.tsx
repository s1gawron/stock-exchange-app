import React, {useEffect, useRef, useState} from 'react';
import styles from "./styles.module.css";
import Topbar from "../../component/topbar/Topbar.tsx";
import Footer from "../../component/footer/Footer.tsx";
import ErrorMsg from "../../component/error/ErrorMsg.tsx";
import {StockSearchResultDTO} from "../../dto/stock/StockSearchDTO.ts";
import {findStock} from "../../util/stocklistings/StockService.ts";
import {isUserAuthenticated} from "../../util/AuthUtil.ts";
import {addFavouriteStock, removeFavouriteStock, getUserFavouriteStocks} from "../../util/favouritestock/FavouriteStockService.ts";
import StockSearchData from "../../component/stockSearch/StockSearchData.tsx";

export default function StockSearchPage(): React.ReactElement {
    const [query, setQuery] = useState<string>("");
    const [results, setResults] = useState<StockSearchResultDTO[]>([]);
    const [errMsg, setErrMsg] = useState<string>("");
    const [loading, setLoading] = useState<boolean>(false);
    const [favouriteTickers, setFavouriteTickers] = useState<Set<string>>(new Set());
    const [pendingTickers, setPendingTickers] = useState<Set<string>>(new Set());
    const timerRef = useRef<ReturnType<typeof setTimeout>>(undefined);
    const userAuthenticated = isUserAuthenticated();

    useEffect(() => {
        if (userAuthenticated) {
            getUserFavouriteStocks().then(res => {
                if (res.success && res.responseBody) {
                    setFavouriteTickers(new Set(res.responseBody.map(f => f.ticker)));
                }
            });
        }
    }, [userAuthenticated]);

    async function toggleFavourite(ticker: string) {
        setPendingTickers(prev => new Set(prev).add(ticker));
        const isFavourite = favouriteTickers.has(ticker);

        if (isFavourite) {
            const res = await removeFavouriteStock(ticker);
            if (res.success) {
                setFavouriteTickers(prev => {
                    const next = new Set(prev);
                    next.delete(ticker);
                    return next;
                });
                setErrMsg("");
            } else {
                setErrMsg(res.errorMsg!);
            }
        } else {
            const res = await addFavouriteStock(ticker);
            if (res.success) {
                setFavouriteTickers(prev => new Set(prev).add(ticker));
                setErrMsg("");
            } else {
                setErrMsg(res.errorMsg!);
            }
        }

        setPendingTickers(prev => {
            const next = new Set(prev);
            next.delete(ticker);
            return next;
        });
    }

    useEffect(() => {
        clearTimeout(timerRef.current);

        if (query.length < 2) {
            setResults([]);
            setErrMsg("");
            setLoading(false);
            return;
        }

        setLoading(true);

        timerRef.current = setTimeout(async () => {
            const res = await findStock(query);

            if (res.success) {
                setResults(res.responseBody!.result);
                setErrMsg("");
            } else {
                setResults([]);
                setErrMsg(res.errorMsg!);
            }

            setLoading(false);
        }, 400);

        return () => clearTimeout(timerRef.current);
    }, [query]);

    return (
        <>
            <Topbar/>
            <div id={styles.errWrapper}><ErrorMsg errMsg={errMsg}/></div>
            <div id={styles.container}>
                <div id={styles.searchWrapper}>
                    <input
                        type="text"
                        placeholder="Search stocks by name or ticker..."
                        value={query}
                        onChange={e => setQuery(e.target.value)}
                        className={styles.searchInput}
                    />
                </div>
                {!errMsg && (
                    <StockSearchData
                        results={results}
                        query={query}
                        loading={loading}
                        userAuthenticated={userAuthenticated}
                        favouriteTickers={favouriteTickers}
                        pendingTickers={pendingTickers}
                        onToggleFavourite={toggleFavourite}
                    />
                )}
            </div>
            <Footer text="Search for stocks across all markets."/>
        </>
    );
}
