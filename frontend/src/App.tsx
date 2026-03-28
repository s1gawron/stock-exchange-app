import React from 'react';
import {BrowserRouter as Router, Route, Routes} from "react-router-dom"
import RegisterPage from "./page/register/RegisterPage.tsx";
import LoginPage from "./page/login/LoginPage.tsx";
import FavouriteStocksPage from "./page/stockListings/FavouriteStocksPage.tsx";
import TransactionPage from "./page/transaction/TransactionPage.tsx";
import WalletPage from "./page/wallet/WalletPage.tsx";
import StockSearchPage from "./page/stockSearch/StockSearchPage.tsx";
import NotFoundPage from "./page/notFound/NotFoundPage.tsx";
import HomePage from "./page/home/HomePage.tsx";

export default function App(): React.ReactElement {
    return (
        <Router>
            <Routes>
                <Route path="/" element={<HomePage/>}/>
                <Route path="/user/register" element={<RegisterPage/>}/>
                <Route path="/user/login" element={<LoginPage/>}/>
                <Route path="/user/wallet" element={<WalletPage/>}/>
                <Route path="/favouriteStocks" element={<FavouriteStocksPage/>}/>
                <Route path="/stockSearch" element={<StockSearchPage/>}/>
                <Route path="/transaction/:ticker" element={<TransactionPage/>}/>
                <Route path="*" element={<NotFoundPage/>}/>
            </Routes>
        </Router>
    );
}
