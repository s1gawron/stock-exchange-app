import React from "react";
import styles from "./styles.module.css";
import {UserWalletDTO} from "../../dto/user/UserWalletDTO.ts";

export default function UserWalletData({userWallet}: { userWallet: UserWalletDTO }): React.ReactElement {
    return (
        <fieldset id={styles.walletInfo}>
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
        </fieldset>
    );
}