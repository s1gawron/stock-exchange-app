import React from 'react';
import {FaHome, FaSearch, FaStar, FaWallet} from "react-icons/fa";
import {useLocation} from "react-router-dom";
import styles from "./styles.module.css";
import LinkButton from "../linkButton/LinkButton.tsx";

export default function Menubar(): React.ReactElement {
    const location = useLocation();

    const isOnTargetPage = (target: string): boolean => {
        return location.pathname.includes(target);
    }

    return (
        <div id={styles.homeLink}>
            <div className={styles.menuBarLink}>
                <LinkButton props={{linkTo: "/", icon: <FaHome size="35px"/>, text: "Home"}}/>
            </div>

            {isOnTargetPage("stockSearch") ? (<></>) :
                (<div className={styles.menuBarLink}>
                    <LinkButton props={{linkTo: "/stockSearch", icon: <FaSearch size="35px"/>, text: "Search"}}/>
                </div>)
            }

            {isOnTargetPage("favouriteStocks") ? (<></>) :
                (<div className={styles.menuBarLink}>
                    <LinkButton props={{linkTo: "/favouriteStocks", icon: <FaStar size="35px"/>, text: "Favourite stocks"}}/>
                </div>)
            }

            {isOnTargetPage("wallet") ? (<></>) :
                (<div className={styles.menuBarLink}>
                    <LinkButton props={{linkTo: "/user/wallet", icon: <FaWallet size="35px"/>, text: "Wallet"}}/>
                </div>)
            }

            <div style={{clear: "both"}}></div>
        </div>
    );
}
