import React from "react";
import {Link} from "react-router-dom";
import styles from "./styles.module.css";
import LinkButtonProperties from "../../dto/linkButton/LinkButtonProperties.ts";

export default function LinkButton({props}: { props: LinkButtonProperties }): React.ReactElement {
    return (
        <Link to={props.linkTo}>
            <button className={styles.linkBtn} style={{fontSize: props.fontSize}}>
                {props.icon !== undefined ? (<div>{props.icon}</div>) : (<></>)}
                <div>{props.text}</div>
            </button>
        </Link>
    );
}
