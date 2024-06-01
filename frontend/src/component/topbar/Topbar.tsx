import React, {useEffect, useState} from "react";
import {FaUser} from "react-icons/fa";
import {LuLogOut} from "react-icons/lu";
import {Link} from "react-router-dom";
import styles from "./styles.module.css";

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
    const day: string = getUnitString(date.getDate());
    const monthName = MONTHS[date.getMonth()];
    const year: number = date.getFullYear();
    const hour: string = getUnitString(date.getHours());
    const minutes: string = getUnitString(date.getMinutes());
    const seconds: string = getUnitString(date.getSeconds());

    return `Today is: ${dayName}, ${day} ${monthName} ${year} ${hour}:${minutes}:${seconds}`;
}

function getUnitString(unit: number): string {
    if (unit < 10) {
        return `0${unit.toString()}`
    }

    return unit.toString();
}

function signOut(): void {
    localStorage.clear();
    window.location.assign('/user/login');
}

function UserSignedInComp(): React.ReactElement {
    const username: string | null = localStorage.getItem('username');

    return (
        <div>
            <div id={styles.usernameDisplay}>{username}</div>
            <div>
                <form onSubmit={(e) => {
                    e.preventDefault();
                    signOut();
                }}>

                    <button id={styles.signOutBtn} type="submit">{<LuLogOut size="18px"/>}</button>
                </form>
            </div>
        </div>
    );
}

function UserNotSignedInComp(): React.ReactElement {
    return (
        <div>
            <div className={styles.userLink}>
                <Link to="/user/login">
                    <button className="userLinkBtn">Sign in</button>
                </Link>
            </div>

            <div className={styles.userLink}>
                <Link to="/user/register">
                    <button className="userLinkBtn">Sign up</button>
                </Link>
            </div>
        </div>
    );
}

export default function Topbar(): React.ReactElement {
    const [date, setDate] = useState<Date>(new Date());
    const isUserLoggedIn: boolean = localStorage.getItem('token') !== null;

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
                    {isUserLoggedIn ? (<UserSignedInComp/>) : (<UserNotSignedInComp/>)}
                </div>
            </div>
        </div>
    );
}