import React from 'react';
import Topbar from "../../component/topbar/Topbar.tsx";
import PageHeader from "../../component/pageHeader/PageHeader.tsx";
import Footer from "../../component/footer/Footer.tsx";
import {Link} from "react-router-dom";
import styles from "./styles.module.css";

export default function NotFoundPage(): React.ReactElement {
    return (
        <>
            <Topbar/>
            <PageHeader text="404 - Page Not Found"/>

            <div id={styles.contentWrapper}>
                <p>The page you are looking for does not exist.</p>
                <Link to="/user/wallet">
                    <button id={styles.userWalletBtn}>Wallet</button>
                </Link>
            </div>

            <Footer text="Looks like you got lost!"/>
        </>
    );
}
