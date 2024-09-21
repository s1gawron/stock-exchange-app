import Topbar from "../../component/topbar/Topbar";
import React, {useEffect, useState} from "react";
import styles from "./styles.module.css";
import {Link} from "react-router-dom";
import {FaChartLine} from "react-icons/fa";
import {TiInfoLarge} from "react-icons/ti";
import {UserWalletDTO} from "../../dto/user/UserWalletDTO";
import {getUserWalletDetails} from "../../util/user/wallet/UserWalletService";
import Footer from "../../component/footer/Footer";
import SplitView from "../../component/splitView/SplitView";
import ErrorMsg from "../../component/error/ErrorMsg";

const UNIX_TIME_ZERO: Date = new Date(1970, 0, 1);

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
    const [errMsg, setErrMsg] = useState<string>("");

    useEffect(() => {
        getUserWalletDetails().then(res => {
            if (res.success) {
                setUserWallet(res.responseBody!);
                return;
            }

            setErrMsg(res.errorMsg!);
        }).catch((error) => {
            console.error("An unexpected error occurred while loading user wallet details:", error);
        });
    }, []);

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
            <p>{userWallet.value.toFixed(2)} USD</p>
            <p>{userWallet.balanceAvailable.toFixed(2)} USD</p>
            <p> {userWallet.balanceBlocked.toFixed(2)} USD</p>
            <p> {userWallet.stockValue.toFixed(2)} USD</p>
            <br/>
            <p> {userWallet.lastDayValue.toFixed(2)} USD</p>
            <p>{userWallet.valuePercentageChange.toFixed(2)}%</p>
            <p>{new Date(userWallet.lastUpdateDate).toLocaleString()}</p>
        </div>

        <div style={{clear: "both"}}></div>
    </fieldset>;

    return (
        <>
            <Topbar/>
            <div id={styles.errWrapper}>
                <ErrorMsg errMsg={errMsg}/>
            </div>
            <SplitView left={left} right={right}/>
            <Footer text="Thank you for trading with us! Here's to your success in the market!"/>
        </>
    );
}
