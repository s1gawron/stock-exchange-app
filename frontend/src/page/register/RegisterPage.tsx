import React, {useState} from "react";
import Topbar from "../../component/topbar/Topbar";
import "./styles.css";
import {UserRegisterDTO} from "../../dto/UserRegisterDTO";
import {registerUser} from "../../util/PublicUserRestService";
import Menubar from "../../component/menubar/Menubar";
import Footer from "../../component/footer/Footer";

const USER_WALLET_BALANCE_DEF_VAL: number = 10_000;

export default function RegisterPage(): React.ReactElement {
    const [userRegister, setUserRegister] = useState<UserRegisterDTO>({
        username: "",
        email: "",
        password: "",
        userWalletBalance: USER_WALLET_BALANCE_DEF_VAL
    });
    const [errMsg, setErrMsg] = useState<string>("")

    return (
        <div>
            <Topbar/>
            <Menubar/>

            <div id="registerHeaderContainer">
                <h3>Sign up now!</h3>
            </div>

            <div id="registerFormWrapper">
                <div className="errors">
                    {errMsg}
                </div>

                <form onSubmit={(e) => {
                    e.preventDefault();
                    registerUser(userRegister, setErrMsg);
                }}>

                    <h4>Username:</h4>
                    <input type="text"
                           className="textInput"
                           required
                           onChange={
                               (e) => setUserRegister((prev) => ({...prev, username: e.target.value}))
                           }/>

                    <h4>E-mail:</h4>
                    <input type="email"
                           className="textInput"
                           required
                           onChange={
                               (e) => setUserRegister((prev) => ({...prev, email: e.target.value}))
                           }/>

                    <h4>Password:</h4>
                    <input type="password"
                           className="textInput"
                           required
                           onChange={
                               (e) => setUserRegister((prev) => ({...prev, password: e.target.value}))
                           }/>

                    <h4>Initial capital:</h4>
                    <select className="selectInput"
                            defaultValue={USER_WALLET_BALANCE_DEF_VAL}
                            onChange={
                                (e) => setUserRegister((prev) => ({...prev, userWalletBalance: Number(e.target.value)}))
                            }>
                        <option value="5000">5000</option>
                        <option value="10000">10000</option>
                        <option value="20000">20000</option>
                    </select>

                    <div>
                        <button id="signUpBtn" type="submit">Sign up!</button>
                    </div>
                </form>
            </div>

            <Footer text="Start your trading journey with us!"/>
        </div>
    );
}
