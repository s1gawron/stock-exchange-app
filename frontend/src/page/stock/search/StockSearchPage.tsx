import React, {useEffect, useState} from 'react';
import styles from "./styles.module.css";
import Topbar from "../../../component/topbar/Topbar.tsx";
import Footer from "../../../component/footer/Footer.tsx";
import {useToast} from "../../../component/toast/ToastContext.ts";
import {UNKNOWN_ERROR_MSG} from "../../../util/ErrorMsgProvider.ts";
import {StockSearchResultDTO} from "../../../dto/stock/StockSearchDTO.ts";
import {findStock} from "../../../util/stocklistings/StockService.ts";
import {isUserAuthenticated} from "../../../util/AuthUtil.ts";
import {addFavouriteStock, removeFavouriteStock, getUserFavouriteStocks} from "../../../util/favouritestock/FavouriteStockService.ts";
import StockSearchData from "../../../component/stock/search/StockSearchData.tsx";

export default function StockSearchPage(): React.ReactElement {
    const [query, setQuery] = useState<string>("");
    const [results, setResults] = useState<StockSearchResultDTO[]>([]);
    const [searchFailed, setSearchFailed] = useState<boolean>(false);
    const [loading, setLoading] = useState<boolean>(false);
    const [favouriteTickers, setFavouriteTickers] = useState<Set<string>>(new Set());
    const [pendingTickers, setPendingTickers] = useState<Set<string>>(new Set());
    const userAuthenticated = isUserAuthenticated();
    const {showError} = useToast();

    useEffect(() => {
        if (userAuthenticated) {
            getUserFavouriteStocks().then(res => {
                if (res.success) {
                    setFavouriteTickers(new Set(res.responseBody!.map(f => f.ticker)));
                    return;
                }

                showError(res.errorMsg!);
            }).catch((error) => {
                console.error("An unexpected error occurred while loading favourite stocks:", error);
                showError(UNKNOWN_ERROR_MSG);
            });
        }
    }, [showError, userAuthenticated]);

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
            } else {
                showError(res.errorMsg!);
            }
        } else {
            const res = await addFavouriteStock(ticker);
            if (res.success) {
                setFavouriteTickers(prev => new Set(prev).add(ticker));
            } else {
                showError(res.errorMsg!);
            }
        }

        setPendingTickers(prev => {
            const next = new Set(prev);
            next.delete(ticker);
            return next;
        });
    }

    useEffect(() => {
        if (query.length < 2) {
            setResults([]);
            setSearchFailed(false);
            setLoading(false);
            return;
        }

        setLoading(true);

        // Aborted when the query changes, so a slower response for an older query cannot overwrite newer results
        const controller = new AbortController();
        const timer = setTimeout(async () => {
            const res = await findStock(query, controller.signal);

            if (controller.signal.aborted) {
                return;
            }

            if (res.success) {
                setResults(res.responseBody!.result);
                setSearchFailed(false);
            } else {
                setResults([]);
                setSearchFailed(true);
                showError(res.errorMsg!);
            }

            setLoading(false);
        }, 400);

        return () => {
            clearTimeout(timer);
            controller.abort();
        };
    }, [query, showError]);

    return (
        <>
            <Topbar/>
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
                {!searchFailed && (
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
