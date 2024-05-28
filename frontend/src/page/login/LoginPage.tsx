import React, {useState} from 'react';
import Topbar from "../../component/topbar/Topbar";
import Menubar from "../../component/menubar/Menubar";
import {Link} from "react-router-dom";
import {loginUser} from "../../util/PublicUserRestService";
import {UserLoginDTO} from "../../dto/UserLoginDTO";

export default function LoginPage(): React.ReactElement {
    const [userLogin, setUserLogin] = useState<UserLoginDTO>({
        username: "",
        password: ""
    });
    const [errMsg, setErrMsg] = useState<string>("");

    return (
        <div>
            <Topbar/>
            <Menubar/>

            <div id="registerHeaderContainer">
                <h3>Sign in!</h3>
            </div>

            <div id="registerFormWrapper">
                <div className="errors">
                    {errMsg}
                </div>

                <form onSubmit={(e) => {
                    e.preventDefault();
                    loginUser(userLogin, setErrMsg);
                }}>

                    <h4>Username:</h4>
                    <input type="text"
                           className="textInput"
                           required
                           onChange={
                               (e) => setUserLogin((prev) => ({...prev, username: e.target.value}))
                           }/>

                    <h4>Password:</h4>
                    <input type="password"
                           className="textInput"
                           required
                           onChange={
                               (e) => setUserLogin((prev) => ({...prev, password: e.target.value}))
                           }/>

                    <div>
                        <button id="signUpBtn" type="submit">Sign up!</button>
                    </div>
                </form>

                <Link to="/user/register">
                    <button className="userLinkBtn">First time? Sign up!</button>
                </Link>
            </div>

            <div id="footer">
                It's good to see you again!
                <br/>
                2024&copy; Sebastian Gawron
            </div>
        </div>
    );
}
