import React from "react";
import {FaHome, FaSearch, FaStar, FaUser, FaWallet} from "react-icons/fa";
import {LuLogOut} from "react-icons/lu";
import styles from "./styles.module.css";
import LinkButton from "../linkButton/LinkButton.tsx";
import {isUserAuthenticated, getUsername, logOut as authLogOut} from "../../util/AuthUtil.ts";
import {useNavigate} from "react-router-dom";

function UserSignedInComp(): React.ReactElement {
    const username = getUsername();
    const navigate = useNavigate();

    return (
        <>
            <div id={styles.usernameDisplay}>{username}</div>
            <div>
                <form onSubmit={(e) => {
                    e.preventDefault();
                    authLogOut();
                    navigate('/user/login');
                }}>

                    <button id={styles.signOutBtn} type="submit">{<LuLogOut size="18px"/>}</button>
                </form>
            </div>
        </>
    );
}

function UserNotSignedInComp(): React.ReactElement {
    return (
        <>
            <div className={styles.userLink}>
                <LinkButton props={{linkTo: "/user/login", text: "Sign in"}}/>
            </div>

            <div className={styles.userLink}>
                <LinkButton props={{linkTo: "/user/register", text: "Sign up"}}/>
            </div>
        </>
    );
}

export default function Topbar(): React.ReactElement {
    const isAuthenticated = isUserAuthenticated();

    return (
        <div id={styles.topbar}>
            <div id={styles.navWrapper}>
                <div className={styles.navLink}>
                    <LinkButton props={{linkTo: "/", icon: <FaHome size="35px"/>, text: "Home"}}/>
                </div>

                <div className={styles.navLink}>
                    <LinkButton props={{linkTo: "/stockSearch", icon: <FaSearch size="35px"/>, text: "Search"}}/>
                </div>

                <div className={styles.navLink}>
                    <LinkButton props={{linkTo: "/favouriteStocks", icon: <FaStar size="35px"/>, text: "Favourite stocks"}}/>
                </div>

                <div className={styles.navLink}>
                    <LinkButton props={{linkTo: "/user/wallet", icon: <FaWallet size="35px"/>, text: "Wallet"}}/>
                </div>
            </div>

            <div id={styles.userWrapper}>
                <div id={styles.userData}>
                    {<FaUser size="35px"/>}
                    {isAuthenticated ? (<UserSignedInComp/>) : (<UserNotSignedInComp/>)}
                </div>
            </div>
        </div>
    );
}
