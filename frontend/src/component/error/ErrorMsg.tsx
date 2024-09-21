import styles from "./styles.module.css";
import React from "react";

export default function ErrorMsg({errMsg}: { errMsg: string }) {
    return (
        <div id={styles.errMsg}>
            {errMsg}
        </div>
    );
}