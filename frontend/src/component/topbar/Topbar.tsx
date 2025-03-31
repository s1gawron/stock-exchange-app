import React, {useEffect, useState} from "react";
import {FaUser} from "react-icons/fa";
import {LuLogOut} from "react-icons/lu";
import styles from "./styles.module.css";
import LinkButton from "../linkButton/LinkButton.tsx";
import AuthUtil from "../../util/AuthUtil.ts";
import RedirectUtil from "../../util/RedirectUtil.ts";

const DAYS: string[] = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
const MONTHS: string[] = ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'];

function getGreeting(hour: number): string {
    if (hour < 12) {
        return 'Good Morning!';
    }

    if (hour < 18) {
        return 'Good Afternoon!';
    }

    return 'Good Evening!';
}

function getClockString(date: Date): string {
    const dayName = DAYS[date.getDay()];
    const day = getUnitString(date.getDate());
    const monthName = MONTHS[date.getMonth()];
    const year = date.getFullYear();
    const hour = getUnitString(date.getHours());
    const minutes = getUnitString(date.getMinutes());
    const seconds = getUnitString(date.getSeconds());

    return `Today is: ${dayName}, ${day} ${monthName} ${year} ${hour}:${minutes}:${seconds}`;
}

function getUnitString(unit: number): string {
    if (unit < 10) {
        return `0${unit.toString()}`
    }

    return unit.toString();
}

function logOut(): void {
    AuthUtil.logOut();
    RedirectUtil.redirectTo('/user/login');
}

function UserSignedInComp(): React.ReactElement {
    const username = AuthUtil.getUsername();

    return (
        <>
            <div id={styles.usernameDisplay}>{username}</div>
            <div>
                <form onSubmit={(e) => {
                    e.preventDefault();
                    logOut();
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
    const [date, setDate] = useState<Date>(new Date());
    const isUserAuthenticated = AuthUtil.isUserAuthenticated();

    useEffect(() => {
        const interval = setInterval(() => setDate(new Date()), 1000);
        return () => {
            clearInterval(interval);
        };
    }, []);

    return (
        <div id={styles.topbar}>
            <div id={styles.timeWrapper}>
                <div id={styles.timeData}>
                    <div id={styles.greeting}>{getGreeting(date.getHours())}</div>
                    <div id={styles.time}>{getClockString(date)}</div>
                </div>
            </div>

            <div id={styles.userWrapper}>
                <div id={styles.userData}>
                    {<FaUser size="35px"/>}
                    {isUserAuthenticated ? (<UserSignedInComp/>) : (<UserNotSignedInComp/>)}
                </div>
            </div>
        </div>
    );
}