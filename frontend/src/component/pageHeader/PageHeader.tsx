import React from "react";
import styles from "./styles.module.css";

export default function PageHeader({text}: { text: string }): React.ReactElement {
    return (
        <div id={styles.pageHeaderContainer}>
            <h3>{text}</h3>
        </div>
    );
}
