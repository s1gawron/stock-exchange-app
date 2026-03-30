import React, {useState} from 'react';
import styles from "./styles.module.css";
import {UserRegisterDTO} from "../../../dto/user/UserRegisterDTO.ts";
import LinkButton from "../../button/link/LinkButton.tsx";
import Header from "../../header/Header.tsx";
import {registerUser} from "../../../util/user/PublicUserService.ts";
import {useNavigate} from "react-router-dom";
import ErrorMsg from "../../error/ErrorMsg.tsx";

const REDIRECT_URL_AFTER_SIGN_UP_SUCCESS: string = "/user/login?fromSignUp=true";

export default function RegisterForm(): React.ReactElement {
    const [values, setValues] = useState<UserRegisterDTO>({username: "", email: "", password: ""});
    const [errMsg, setErrMsg] = useState<string>("");
    const navigate = useNavigate();

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const {name, value} = e.target;
        setValues(prev => ({...prev, [name]: value}));
    };

    const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        registerUser(values).then(res => {
                if (res.success) {
                    navigate(REDIRECT_URL_AFTER_SIGN_UP_SUCCESS);
                    setErrMsg("");
                    return;
                }

                setErrMsg(res.errorMsg!);
            }
        ).catch((error) => {
            console.error("An unexpected error occurred while registering user:", error);
        });
    };

    return (
        <div id={styles.formWrapper}>
            <div id={styles.errWrapper}>
                <ErrorMsg errMsg={errMsg}/>
            </div>

            <Header text="Sign up now!"/>

            <form onSubmit={handleSubmit}>
                <div className={styles.formElementWrapper}>
                    <input type="text" name="username" placeholder="Username" value={values.username} onChange={handleChange} className={styles.textInput}
                           required/>
                </div>

                <div className={styles.formElementWrapper}>
                    <input type="email" name="email" placeholder="Email" value={values.email} onChange={handleChange} className={styles.textInput} required/>
                </div>

                <div className={styles.formElementWrapper}>
                    <input type="password" name="password" placeholder="Password" value={values.password} onChange={handleChange} className={styles.textInput}
                           required/>
                </div>

                <div className={styles.formElementWrapper}>
                    <button id={styles.submitBtn} type="submit">Sign up!</button>
                </div>
            </form>

            <div id={styles.linkBtnWrapper}>
                <LinkButton props={{linkTo: "/user/login", text: "Already have an account? Sign in!"}}/>
            </div>
        </div>
    );
}
