import React from "react";
import styles from "./styles.module.css";

export default function ErrorMsg({errMsg}: { errMsg: string }): React.ReactElement {
    return (
        <div id={styles.errMsg}>
            {errMsg}
        </div>
    );
}