import Topbar from "../../component/topbar/Topbar";
import React, {useEffect, useState} from "react";
import styles from "./styles.module.css";
import {Link} from "react-router-dom";
import {FaChartLine} from "react-icons/fa";
import {TiInfoLarge} from "react-icons/ti";
import {UserWalletDTO} from "../../dto/user/UserWalletDTO";
import UserWalletService from "../../util/user/wallet/UserWalletService";
import Footer from "../../component/footer/Footer";
import SplitView from "../../component/splitView/SplitView";

const UNIX_TIME_ZERO: Date = new Date(1970, 0, 1);

function numToFixed(num: number): string {
    return num.toFixed(2);
}

export default function HomePage(): React.ReactElement {
    const [userWallet, setUserWallet] = useState<UserWalletDTO>({
        stockValue: 0.00,
        balanceAvailable: 0.00,
        balanceBlocked: 0.00,
        value: 0.00,
        lastDayValue: 0.00,
        valuePercentageChange: 0.00,
        lastUpdateDate: UNIX_TIME_ZERO.toLocaleString()
    });

    useEffect(() => UserWalletService.getUserWalletDetails(setUserWallet), []);

    const left: React.ReactElement = <fieldset id={styles.actions}>
        <legend>Actions:</legend>

        <div className={styles.actionBtnWrapper}>
            <Link to="/stockListings/DJI">
                <button id={styles.listingsBtn} className={styles.actionBtn}>
                    <div>Stock listings</div>
                    <div><FaChartLine className={styles.actionIcons}/></div>
                </button>
            </Link>
        </div>

        <div className={styles.actionBtnWrapper}>
            <Link to="/user/myWallet">
                <button id={styles.walletDetailsBtn} className={styles.actionBtn}>
                    <div>Wallet details</div>
                    <div><TiInfoLarge className={styles.actionIcons}/></div>
                </button>
            </Link>
        </div>
    </fieldset>;

    const right: React.ReactElement = <fieldset id={styles.walletInfo}>
        <legend>My wallet:</legend>
        <div id={styles.walletLegend} className={styles.walletInfoFont}>
            <p>Wallet value:</p>
            <p>Balance available:</p>
            <p>Balance blocked:</p>
            <p>Stock value:</p>
            <br/>
            <p>Previous wallet value:</p>
            <p>Wallet percentage change:</p>
            <p>Update date:</p>
        </div>

        <div id={styles.walletData} className={styles.walletInfoFont}>
            <p>{numToFixed(userWallet.value)} USD</p>
            <p>{numToFixed(userWallet.balanceAvailable)} USD</p>
            <p> {numToFixed(userWallet.balanceBlocked)} USD</p>
            <p> {numToFixed(userWallet.stockValue)} USD</p>
            <br/>
            <p> {numToFixed(userWallet.lastDayValue)} USD</p>
            <p>{numToFixed(userWallet.valuePercentageChange)}%</p>
            <p>{new Date(userWallet.lastUpdateDate).toLocaleString()}</p>
        </div>

        <div style={{clear: "both"}}></div>
    </fieldset>;

    return (
        <>
            <Topbar/>
            <SplitView left={left} right={right}/>
            <Footer text="Thank you for trading with us! Here's to your success in the market!"/>
        </>
    );
}
