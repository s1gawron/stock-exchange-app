import Topbar from "../../component/topbar/Topbar";
import Menubar from "../../component/menubar/Menubar";
import styles from "./styles.module.css";
import ErrorMsg from "../../component/error/ErrorMsg";
import React, {useEffect, useState} from "react";
import Footer from "../../component/footer/Footer";
import {getUserStocks, getUserWalletDetails} from "../../util/user/wallet/UserWalletService";
import {UserStockDTO} from "../../dto/user/UserStockDTO";
import UserStockData from "../../component/userStock/UserStockData";
import UserWalletData from "../../component/userWallet/UserWalletData";
import {UserWalletDTO} from "../../dto/user/UserWalletDTO";
import AuthUtil from "../../util/AuthUtil";
import RedirectUtil from "../../util/RedirectUtil";

const REDIRECT_TO_LOGIN_PAGE: string = "/user/login";

export default function WalletPage(): React.ReactElement {
    const [errMsg, setErrMsg] = useState<string>("");
    const [userStocks, setUserStocks] = useState<UserStockDTO[]>();
    const [userWallet, setUserWallet] = useState<UserWalletDTO>();

    useEffect(() => {
        if (AuthUtil.isUserNotAuthenticated()) {
            RedirectUtil.redirectTo(REDIRECT_TO_LOGIN_PAGE);
            return
        }

        getUserStocks().then(res => {
            if (res.success) {
                setUserStocks(res.responseBody!);
                return;
            }

            setErrMsg(res.errorMsg!);
        }).catch((error) => {
            console.error("An unexpected error occurred while loading user stocks:", error);
        });

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

    return (
        <>
            <Topbar/>
            <Menubar/>

            <div id={styles.errWrapper}>
                <ErrorMsg errMsg={errMsg}/>
            </div>

            <div id={styles.container}>
                {userStocks === undefined ? (
                    <div className={styles.loaderWrapper}>
                        <div className={styles.loader}></div>
                    </div>
                ) : <UserStockData userStocks={userStocks}/>}
            </div>

            <div id={styles.userWalletInfo}>
                {userWallet !== undefined && <UserWalletData userWallet={userWallet}/>}
            </div>

            <Footer text="Your finances at a glance."/>
        </>
    );
}