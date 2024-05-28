import React from 'react';
import {Link} from "react-router-dom";
import {FaHome} from "react-icons/fa";
import "./styles.css";

export default function Menubar(): React.ReactElement {
    return (
        <div id="homeLink">
            <div className="menuBarLink">
                <Link to="/">
                    <button className="userLinkBtn">
                        <div>{<FaHome size="35px"/>}</div>
                        <div>Home</div>
                    </button>
                </Link>
            </div>
        </div>
    );
}
