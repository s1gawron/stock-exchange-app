import React, {useState} from 'react';
import styles from "./styles.module.css";
import {UserRegisterDTO} from "../../dto/user/UserRegisterDTO.ts";
import LinkButton from "../linkButton/LinkButton.tsx";

interface RegisterFormProps {
    onSubmit: (values: UserRegisterDTO) => void;
}

export default function RegisterForm({onSubmit}: RegisterFormProps): React.ReactElement {
    const [values, setValues] = useState<UserRegisterDTO>({username: "", email: "", password: ""});

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
                    <h4>E-mail:</h4>
                    <input type="email" name="email" value={values.email} onChange={handleChange} className={styles.textInput} required/>
                </div>
                <div>
                    <h4>Password:</h4>
                    <input type="password" name="password" value={values.password} onChange={handleChange} className={styles.textInput} required/>
                </div>
                <div>
                    <button id={styles.submitBtn} type="submit">Sign up!</button>
                </div>
            </form>
            <div id={styles.linkBtnWrapper}>
                <LinkButton props={{linkTo: "/user/login", text: "Already have an account? Sign in!"}}/>
            </div>
        </div>
    );
}
