import React, {useEffect} from "react";
import Topbar from "../../component/topbar/Topbar";
import Menubar from "../../component/menubar/Menubar";
import AuthUtil from "../../util/AuthUtil";
import RedirectUtil from "../../util/RedirectUtil";
import AbstractForm from "../../component/form/AbstractForm";
import styles from "./styles.module.css";
import SplitView from "../../component/splitView/SplitView";
import Footer from "../../component/footer/Footer";

export default function TransactionPage(): React.ReactElement {
    useEffect(() => {
        if (!AuthUtil.isUserNotAuthenticated()) {
            RedirectUtil.redirectTo("/user/login");
        }
    }, []);

    const left: React.ReactElement =
        <div id={styles.stockInfoWrapper}>
            <fieldset id={styles.stockInfo}>
                <legend> Stock info:</legend>
                <div id={styles.stockInfoLegend} className={styles.stockInfoFont}>
                    <p>Ticker:</p>
                    <p>Company full name:</p>
                    <p>Industry:</p>
                    <p>Company origin country:</p>
                    <p>Stock exchange:</p>
                    <p>Market capitalization:</p>
                    <br/>
                    <p>Current price:</p>
                    <p>Previous price:</p>
                    <p>Price change:</p>
                    <p>Percentage price change:</p>
                    <p>Update date:</p>
                </div>

                <div id={styles.stockData} className={styles.stockInfoFont}>
                    <p>AAPL</p>
                    <p>Apple</p>
                    <p>IT</p>
                    <p>USA</p>
                    <p>NYSE</p>
                    <p>100201392103</p>
                    <br/>
                    <p>28,98</p>
                    <p>25,00</p>
                    <p>2,00</p>
                    <p>0.1%</p>
                    <p>1.01.1970, 00:00:00</p>
                </div>

                <div style={{clear: "both"}}></div>
            </fieldset>
        </div>

    const right: React.ReactElement =
        <div>
            <AbstractForm
                initialValues={''}
                onSubmit={() => {
                }}
                fields={[
                    {
                        name: "transactionType",
                        type: "radio",
                        label: "Transaction type",
                        options: [{value: "BUY", label: "Buy"}, {value: "SELL", label: "Sell"}]
                    },
                    {name: "price", type: "number", label: "Price"},
                    {name: "quantity", type: "number", label: "Quantity"},
                ]}
                submitButtonText="Create transaction!"
                errorMessage={""}
            />
        </div>

    return (
        <>
            <Topbar/>
            <Menubar/>
            <div id={styles.splitViewWrapper}>
                <SplitView left={left} right={right}/>
            </div>
            <Footer text="Indeed, a wise choice!"/>
        </>
    );
}
