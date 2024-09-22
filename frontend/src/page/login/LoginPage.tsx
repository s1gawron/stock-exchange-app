import React, {useState} from 'react';
import Topbar from "../../component/topbar/Topbar";
import Menubar from "../../component/menubar/Menubar";
import {logInUser} from "../../util/user/PublicUserService";
import {UserLoginDTO} from "../../dto/user/UserLoginDTO";
import Footer from "../../component/footer/Footer";
import AbstractForm from "../../component/form/AbstractForm";
import PageHeader from "../../component/pageHeader/PageHeader";
import AuthUtil from "../../util/AuthUtil";
import RedirectUtil from "../../util/RedirectUtil";
import styles from "./styles.module.css";
import ErrorMsg from "../../component/error/ErrorMsg";

const REDIRECT_URL_AFTER_SIGN_IN_SUCCESS: string = "/";

export default function LoginPage(): React.ReactElement {
    const initialValues: UserLoginDTO = {
        username: "",
        password: ""
    };
    const [errMsg, setErrMsg] = useState<string>("");

    const handleSubmit = (values: UserLoginDTO) => {
        logInUser(values).then(res => {
            if (res.success) {
                AuthUtil.logIn(res.responseBody!.username, res.responseBody!.token);
                RedirectUtil.redirectTo(REDIRECT_URL_AFTER_SIGN_IN_SUCCESS);
                setErrMsg("");
                return;
            }

            setErrMsg(res.errorMsg!);
        }).catch((error) => {
            console.error("An unexpected error occurred while registering user:", error);
        });
    };

    return (
        <>
            <Topbar/>
            <Menubar/>
            <PageHeader text="Sign in!"/>

            <div id={styles.errWrapper}>
                <ErrorMsg errMsg={errMsg}/>
            </div>

            <AbstractForm
                initialValues={initialValues}
                onSubmit={handleSubmit}
                fields={[
                    {name: 'username', type: 'text', label: 'Username'},
                    {name: 'password', type: 'password', label: 'Password'},
                ]}
                submitButtonText="Sign in!"
                formLink={{to: "/user/register", text: "First time? Sign up!"}}
            />

            <Footer text="It's good to see you again!"/>
        </>
    );
}
