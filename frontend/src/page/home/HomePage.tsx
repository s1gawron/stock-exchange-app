import Topbar from "../../component/topbar/Topbar";
import React, {useState} from "react";
import "./styles.css";
import {Link} from "react-router-dom";
import {FaChartLine} from "react-icons/fa";
import {TiInfoLarge} from "react-icons/ti";

const UNIX_TIME_ZERO: Date = new Date(1970, 0, 1);

interface UserWalletDTO {
    stockValue: number;
    balanceAvailable: number;
    balanceBlocked: number;
    value: number;
    lastDayValue: number;
    valuePercentageChange: number;
    lastUpdateDate: string;
}

function numToFixed(num: number): string {
    return num.toFixed(2);
}

export default function HomePage(): React.ReactElement {
    const [userWallet, setUserWallet] = useState<UserWalletDTO>({
        stockValue: 0.00,
        balanceAvailable: 0.00,
        balanceBlocked: 0.00,
        value: 0.00,
        lastDayValue: 0.00,
        valuePercentageChange: 0.00,
        lastUpdateDate: UNIX_TIME_ZERO.toLocaleString()
    });

    return (
        <div>
            <Topbar/>

            <div id="container">
                <div className="square">
                    <fieldset id="actions">
                        <legend>Actions:</legend>

                        <div className="actionBtnWrapper">
                            <Link to="/stockListings/SP500">
                                <button id="listingsBtn" className="actionBtn">
                                    <div>Stock listings</div>
                                    <div><FaChartLine className="actionIcons"/></div>
                                </button>
                            </Link>
                        </div>

                        <div className="actionBtnWrapper">
                            <Link to="/user/myWallet">
                                <button id="walletDetailsBtn" className="actionBtn">
                                    <div>Wallet details</div>
                                    <div><TiInfoLarge className="actionIcons"/></div>
                                </button>
                            </Link>
                        </div>
                    </fieldset>
                </div>

                <div className="square">
                    <fieldset id="walletInfo">
                        <legend>My wallet:</legend>
                        <div id="walletLegend" className="walletInfoFont">
                            <p>Wallet value:</p>
                            <p>Balance available:</p>
                            <p>Balance blocked:</p>
                            <p>Stock value:</p>
                            <br/>
                            <p>Previous wallet value:</p>
                            <p>Wallet percentage change:</p>
                            <p>Update date:</p>
                        </div>

                        <div id="walletData" className="walletInfoFont">
                            <p>{numToFixed(userWallet.value)} USD</p>
                            <p>{numToFixed(userWallet.balanceAvailable)} USD</p>
                            <p> {numToFixed(userWallet.balanceBlocked)} USD</p>
                            <p> {numToFixed(userWallet.stockValue)} USD</p>
                            <br/>
                            <p> {numToFixed(userWallet.lastDayValue)} USD</p>
                            <p>{numToFixed(userWallet.valuePercentageChange)}%</p>
                            <p>{userWallet.lastUpdateDate}</p>
                        </div>

                        <div style={{clear: "both"}}></div>
                    </fieldset>
                </div>

                <div style={{clear: "both"}}></div>
            </div>

            <div id="footer">
                Thank you for trading with us! Here's to your success in the market!
                <br/>
                2024&copy; Sebastian Gawron
            </div>
        </div>
    );
}
