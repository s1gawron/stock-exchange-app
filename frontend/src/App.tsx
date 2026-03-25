import React from 'react';
import {BrowserRouter as Router, Route, Routes} from "react-router-dom"
import HomePage from "./page/home/HomePage.tsx";
import RegisterPage from "./page/register/RegisterPage.tsx";
import LoginPage from "./page/login/LoginPage.tsx";
import StockListingsPage from "./page/stockListings/StockListingsPage.tsx";
import TransactionPage from "./page/transaction/TransactionPage.tsx";
import WalletPage from "./page/wallet/WalletPage.tsx";
import NotFoundPage from "./page/notFound/NotFoundPage.tsx";

export default function App(): React.ReactElement {
    return (
        <Router>
            <Routes>
                <Route path="/" element={<HomePage/>}/>
                <Route path="/user/register" element={<RegisterPage/>}/>
                <Route path="/user/login" element={<LoginPage/>}/>
                <Route path="/user/wallet" element={<WalletPage/>}/>
                <Route path="/stockListings/:index" element={<StockListingsPage/>}/>
                <Route path="/transaction/:ticker" element={<TransactionPage/>}/>
                <Route path="*" element={<NotFoundPage/>}/>
            </Routes>
        </Router>
    );
}
