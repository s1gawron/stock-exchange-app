import Topbar from "../../component/topbar/Topbar.tsx";
import React, {useEffect, useState} from "react";
import styles from "./styles.module.css";
import {Link} from "react-router-dom";
import {FaChartLine} from "react-icons/fa";
import {TiInfoLarge} from "react-icons/ti";
import {UserWalletDTO} from "../../dto/user/UserWalletDTO.ts";
import {getUserWalletDetails} from "../../util/user/wallet/UserWalletService.ts";
import Footer from "../../component/footer/Footer.tsx";
import SplitView from "../../component/splitView/SplitView.tsx";
import ErrorMsg from "../../component/error/ErrorMsg.tsx";
import UserWalletData from "../../component/userWallet/UserWalletData.tsx";

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

    const left = <fieldset id={styles.actions}>
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
            <Link to="/user/wallet">
                <button id={styles.walletDetailsBtn} className={styles.actionBtn}>
                    <div>Wallet details</div>
                    <div><TiInfoLarge className={styles.actionIcons}/></div>
                </button>
            </Link>
        </div>
    </fieldset>;

    const right = <UserWalletData userWallet={userWallet}/>

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
