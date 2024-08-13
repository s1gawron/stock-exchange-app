import React from 'react';
import {FaHome} from "react-icons/fa";
import styles from "./styles.module.css";
import LinkButton from "../linkButton/LinkButton";

export default function Menubar(): React.ReactElement {
    return (
        <div id={styles.homeLink}>
            <div className={styles.menuBarLink}>
                <LinkButton props={{linkTo: "/", icon: <FaHome size="35px"/>, text: "Home"}}/>
            </div>
        </div>
    );
}
