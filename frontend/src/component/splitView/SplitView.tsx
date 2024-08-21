import React from "react";
import styles from "./styles.module.css";

export default function SplitView({left, right}: { left: React.ReactNode, right: React.ReactNode }): React.ReactElement {
    return (
        <div id={styles.container}>
            <div className={styles.square}>
                {left}
            </div>

            <div id={styles.right} className={styles.square}>
                {right}
            </div>

            <div style={{clear: "both"}}></div>
        </div>
    );
}