import React from "react";
import Footer from "../../component/footer/Footer.tsx";
import Header from "../../component/header/Header.tsx";
import Topbar from "../../component/topbar/Topbar.tsx";

export default function HomePage(): React.ReactElement {
    return (
        <>
            <Topbar/>
            <Header text="Stock Exchange Simulator"/>
            <Footer text="Thank you for trading with us! Here's to your success in the market!"/>
        </>
    );
}
