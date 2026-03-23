import React, {useState} from 'react';
import styles from "./styles.module.css";
import {UserLoginDTO} from "../../dto/user/UserLoginDTO.ts";
import LinkButton from "../linkButton/LinkButton.tsx";

interface LoginFormProps {
    onSubmit: (values: UserLoginDTO) => void;
}

export default function LoginForm({onSubmit}: LoginFormProps): React.ReactElement {
    const [values, setValues] = useState<UserLoginDTO>({username: "", password: ""});

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const {name, value} = e.target;
        setValues(prev => ({...prev, [name]: value}));
    };

    const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        onSubmit(values);
    };

    return (
        <div id={styles.formWrapper}>
            <form onSubmit={handleSubmit}>
                <div>
                    <h4>Username:</h4>
                    <input type="text" name="username" value={values.username} onChange={handleChange} className={styles.textInput} required/>
                </div>
                <div>
                    <h4>Password:</h4>
                    <input type="password" name="password" value={values.password} onChange={handleChange} className={styles.textInput} required/>
                </div>
                <div>
                    <button id={styles.submitBtn} type="submit">Sign in!</button>
                </div>
            </form>
            <div id={styles.linkBtnWrapper}>
                <LinkButton props={{linkTo: "/user/register", text: "First time? Sign up!"}}/>
            </div>
        </div>
    );
}
