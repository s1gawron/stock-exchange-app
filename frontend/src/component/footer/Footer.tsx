import React from 'react';
import styles from "./styles.module.css";

export default function Footer({text}: { text: string }): React.ReactElement {
    return (
        <div id={styles.footer}>
            {text}
            <br/>
            2024&copy; Sebastian Gawron
        </div>
    );
}
