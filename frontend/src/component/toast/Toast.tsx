import React from "react";
import {FaTimes} from "react-icons/fa";
import styles from "./styles.module.css";
import {ToastType} from "./ToastContext.ts";

export default function Toast({type, msg, onClose}: { type: ToastType, msg: string, onClose: () => void }): React.ReactElement {
    const typeClass = type === ToastType.SUCCESS ? styles.success : styles.error;

    return (
        <div className={`${styles.toast} ${typeClass}`} role={type === ToastType.SUCCESS ? "status" : "alert"}>
            <div className={styles.toastMsg}>{msg}</div>
            <button className={styles.closeBtn} onClick={onClose} aria-label="Close">
                <FaTimes size="14px"/>
            </button>
        </div>
    );
}
