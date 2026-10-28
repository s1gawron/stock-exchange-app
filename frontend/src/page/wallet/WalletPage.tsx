import Topbar from "../../component/topbar/Topbar.tsx";
import styles from "./styles.module.css";
import {useToast} from "../../component/toast/ToastContext.ts";
import {UNKNOWN_ERROR_MSG} from "../../util/ErrorMsgProvider.ts";
import React, {useEffect, useState} from "react";
import Footer from "../../component/footer/Footer.tsx";
import {getUserStocks, getUserWalletDetails} from "../../util/user/wallet/UserWalletService.ts";
import {UserStockDTO} from "../../dto/user/UserStockDTO.ts";
import UserStockData from "../../component/user/stock/UserStockData.tsx";
import UserWalletData from "../../component/user/wallet/UserWalletData.tsx";
import {UserWalletDTO} from "../../dto/user/UserWalletDTO.ts";

export default function WalletPage(): React.ReactElement {
    const {showError} = useToast();
    const [userStocks, setUserStocks] = useState<UserStockDTO[]>();
    const [userWallet, setUserWallet] = useState<UserWalletDTO>();
    const [loading, setLoading] = useState<boolean>(true);

    useEffect(() => {
        getUserStocks().then(res => {
            if (res.success) {
                setUserStocks(res.responseBody!);
            } else {
                showError(res.errorMsg!);
            }

            setLoading(false);
        }).catch((error) => {
            console.error("An unexpected error occurred while loading user stocks:", error);
            showError(UNKNOWN_ERROR_MSG);
            setLoading(false);
        });

        getUserWalletDetails().then(res => {
            if (res.success) {
                setUserWallet(res.responseBody!);
                return;
            }

            showError(res.errorMsg!);
        }).catch((error) => {
            console.error("An unexpected error occurred while loading user wallet details:", error);
            showError(UNKNOWN_ERROR_MSG);
        });
    }, [showError]);

    return (
        <>
            <Topbar/>

            <div id={styles.container}>
                {loading ? (
                    <div className={styles.loaderWrapper}>
                        <div className={styles.loader}></div>
                    </div>
                ) : userStocks !== undefined && <UserStockData userStocks={userStocks}/>}
            </div>

            <div id={styles.userWalletInfo}>
                {userWallet !== undefined && <UserWalletData userWallet={userWallet}/>}
            </div>

            <Footer text="Your finances at a glance."/>
        </>
    );
}