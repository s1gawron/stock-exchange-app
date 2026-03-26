import React, {useEffect, useRef, useState} from 'react';
import {Link} from "react-router-dom";
import styles from "./styles.module.css";
import Topbar from "../../component/topbar/Topbar.tsx";
import Menubar from "../../component/menubar/Menubar.tsx";
import Footer from "../../component/footer/Footer.tsx";
import ErrorMsg from "../../component/error/ErrorMsg.tsx";
import {StockSearchResultDTO} from "../../dto/stock/StockSearchDTO.ts";
import {findStock} from "../../util/stocklistings/StockService.ts";

export default function StockSearchPage(): React.ReactElement {
    const [query, setQuery] = useState<string>("");
    const [results, setResults] = useState<StockSearchResultDTO[]>([]);
    const [errMsg, setErrMsg] = useState<string>("");
    const [loading, setLoading] = useState<boolean>(false);
    const timerRef = useRef<ReturnType<typeof setTimeout>>(undefined);

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

    const rows = results.map((result, index) => (
        <tr key={result.symbol + index}>
            <td className={`${styles.stockSearchCell} ${styles.stockSearchTd} ${styles.oddTableData}`}>{result.displaySymbol}</td>
            <td className={`${styles.stockSearchCell} ${styles.stockSearchTd}`}>{result.description}</td>
            <td className={`${styles.stockSearchCell} ${styles.stockSearchTd} ${styles.oddTableData}`}>{result.type}</td>
            <td className={`${styles.stockSearchCell} ${styles.stockSearchTd}`}>
                <div className={styles.buttonWrapper}>
                    <Link to={`/transaction/${result.symbol}`}>
                        <button className={styles.actionButton}>Buy/Sell</button>
                    </Link>
                </div>
            </td>
        </tr>
    ));

    return (
        <>
            <Topbar/>
            <Menubar/>
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

                {loading ? (
                    <div className={styles.loaderWrapper}>
                        <div className={styles.loader}></div>
                    </div>
                ) : results.length === 0 && query.length >= 2 && !errMsg ? (
                    <div id={styles.noResults}>No results found for "{query}"</div>
                ) : results.length > 0 ? (
                    <table id={styles.stockSearchTable}>
                        <thead>
                        <tr>
                            <th className={`${styles.stockSearchCell} ${styles.stockSearchTh}`}>Symbol</th>
                            <th className={`${styles.stockSearchCell} ${styles.stockSearchTh}`}>Description</th>
                            <th className={`${styles.stockSearchCell} ${styles.stockSearchTh}`}>Type</th>
                            <th className={`${styles.stockSearchCell} ${styles.stockSearchTh}`}></th>
                        </tr>
                        </thead>
                        <tbody>
                        {rows}
                        </tbody>
                    </table>
                ) : null}
            </div>
            <Footer text="Search for stocks across all markets."/>
        </>
    );
}
