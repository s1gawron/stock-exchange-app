import React from 'react';
import {FaChartLine, FaHome} from "react-icons/fa";
import styles from "./styles.module.css";
import LinkButton from "../linkButton/LinkButton.tsx";
import {TiInfoLarge} from "react-icons/ti";

export default function Menubar(): React.ReactElement {

    const isOnTargetPage = (target: string): boolean => {
        const currentLocation: string = window.location.pathname;
        return currentLocation.includes(target);
    }

    return (
        <div id={styles.homeLink}>
            <div className={styles.menuBarLink}>
                <LinkButton props={{linkTo: "/", icon: <FaHome size="35px"/>, text: "Home"}}/>
            </div>

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
