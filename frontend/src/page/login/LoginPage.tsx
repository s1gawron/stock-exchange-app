import React, {useState} from 'react';
import Topbar from "../../component/topbar/Topbar.tsx";
import Menubar from "../../component/menubar/Menubar.tsx";
import {logInUser} from "../../util/user/PublicUserService.ts";
import {UserLoginDTO} from "../../dto/user/UserLoginDTO.ts";
import Footer from "../../component/footer/Footer.tsx";
import LoginForm from "../../component/form/LoginForm.tsx";
import PageHeader from "../../component/pageHeader/PageHeader.tsx";
import {logIn} from "../../util/AuthUtil.ts";
import {redirectTo} from "../../util/RedirectUtil.ts";
import styles from "./styles.module.css";
import ErrorMsg from "../../component/error/ErrorMsg.tsx";

const REDIRECT_URL_AFTER_SIGN_IN_SUCCESS: string = "/";

export default function LoginPage(): React.ReactElement {
    const [errMsg, setErrMsg] = useState<string>("");

    const handleSubmit = (values: UserLoginDTO): void => {
        logInUser(values).then(res => {
            if (res.success) {
                logIn(res.responseBody!.username, res.responseBody!.token);
                redirectTo(REDIRECT_URL_AFTER_SIGN_IN_SUCCESS);
                setErrMsg("");
                return;
            }

            setErrMsg(res.errorMsg!);
        }).catch((error) => {
            console.error("An unexpected error occurred while logging user:", error);
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

            <LoginForm onSubmit={handleSubmit}/>

            <Footer text="It's good to see you again!"/>
        </>
    );
}
