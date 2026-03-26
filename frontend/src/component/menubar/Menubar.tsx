import React from 'react';
import {FaChartLine, FaHome, FaSearch} from "react-icons/fa";
import {useLocation} from "react-router-dom";
import styles from "./styles.module.css";
import LinkButton from "../linkButton/LinkButton.tsx";
import {TiInfoLarge} from "react-icons/ti";

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

            {isOnTargetPage("stockListings") ? (<></>) :
                (<div className={styles.menuBarLink}>
                    <LinkButton props={{linkTo: "/stockListings/DJI", icon: <FaChartLine size="35px"/>, text: "Listings"}}/>
                </div>)
            }

            {isOnTargetPage("wallet") ? (<></>) :
                (<div className={styles.menuBarLink}>
                    <LinkButton props={{linkTo: "/user/wallet", icon: <TiInfoLarge size="35px"/>, text: "Wallet"}}/>
                </div>)
            }

            <div style={{clear: "both"}}></div>
        </div>
    );
}
