import React from "react";
import {FaTimes} from "react-icons/fa";
import styles from "./styles.module.css";

export default function Toast({msg, onClose}: { msg: string, onClose: () => void }): React.ReactElement {
    return (
        <div className={styles.toast} role="alert">
            <div className={styles.toastMsg}>{msg}</div>
            <button className={styles.closeBtn} onClick={onClose} aria-label="Close">
                <FaTimes size="14px"/>
            </button>
        </div>
    );
}
