import React from 'react';
import styles from "./styles.module.css";

const COPYRIGHT_START_YEAR: number = 2019;

export default function Footer({text}: { text: string }): React.ReactElement {
    return (
        <div id={styles.footer}>
            {text}
            <br/>
            {COPYRIGHT_START_YEAR}-{new Date().getFullYear()}&copy; Sebastian Gawron
        </div>
    );
}
