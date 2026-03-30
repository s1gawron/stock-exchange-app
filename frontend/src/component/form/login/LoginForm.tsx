import React, {useState} from 'react';
import styles from "./styles.module.css";
import {UserLoginDTO} from "../../../dto/user/UserLoginDTO.ts";
import LinkButton from "../../button/link/LinkButton.tsx";
import Header from "../../header/Header.tsx";
import ErrorMsg from "../../error/ErrorMsg.tsx";
import {useNavigate} from "react-router-dom";
import {logInUser} from "../../../util/user/PublicUserService.ts";
import {logIn} from "../../../util/AuthUtil.ts";

const REDIRECT_URL_AFTER_SIGN_IN_SUCCESS: string = "/user/wallet";

export function LoginForm(): React.ReactElement {
    const [values, setValues] = useState<UserLoginDTO>({username: "", password: ""});
    const [errMsg, setErrMsg] = useState<string>("");
    const navigate = useNavigate();

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const {name, value} = e.target;
        setValues(prev => ({...prev, [name]: value}));
    };

    const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        logInUser(values).then(res => {
            if (res.success) {
                logIn(res.responseBody!.username, res.responseBody!.token);
                navigate(REDIRECT_URL_AFTER_SIGN_IN_SUCCESS);
                setErrMsg("");
                return;
            }

            setErrMsg(res.errorMsg!);
        }).catch((error) => {
            console.error("An unexpected error occurred while logging user:", error);
        });
    };

    return (
        <div id={styles.formWrapper}>
            <div id={styles.errWrapper}>
                <ErrorMsg errMsg={errMsg}/>
            </div>

            <Header text="Sign in!"/>

            <form onSubmit={handleSubmit}>
                <div className={styles.formElementWrapper}>
                    <input type="text" name="username" placeholder="Username" value={values.username} onChange={handleChange} className={styles.textInput}
                           required/>
                </div>

                <div className={styles.formElementWrapper}>
                    <input type="password" name="password" placeholder="Password" value={values.password} onChange={handleChange} className={styles.textInput}
                           required/>
                </div>

                <div className={styles.formElementWrapper}>
                    <button id={styles.submitBtn} type="submit">Sign in!</button>
                </div>
            </form>

            <div id={styles.linkBtnWrapper}>
                <LinkButton props={{linkTo: "/user/register", text: "First time? Sign up!"}}/>
            </div>
        </div>
    );
}
