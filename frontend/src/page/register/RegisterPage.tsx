import React, {useState} from "react";
import Topbar from "../../component/topbar/Topbar.tsx";
import {UserRegisterDTO} from "../../dto/user/UserRegisterDTO.ts";
import {registerUser} from "../../util/user/PublicUserService.ts";
import Menubar from "../../component/menubar/Menubar.tsx";
import Footer from "../../component/footer/Footer.tsx";
import AbstractForm from "../../component/form/AbstractForm.tsx";
import PageHeader from "../../component/pageHeader/PageHeader.tsx";
import RedirectUtil from "../../util/RedirectUtil.ts";
import styles from "./styles.module.css";
import ErrorMsg from "../../component/error/ErrorMsg.tsx";

const REDIRECT_URL_AFTER_SIGN_UP_SUCCESS: string = "/user/login?fromSignUp=true";

export default function RegisterPage(): React.ReactElement {
    const initialValues: UserRegisterDTO = {
        username: "",
        email: "",
        password: ""
    };
    const [errMsg, setErrMsg] = useState<string>("");

    const handleSubmit = (values: UserRegisterDTO) => {
        registerUser(values).then(res => {
                if (res.success) {
                    RedirectUtil.redirectTo(REDIRECT_URL_AFTER_SIGN_UP_SUCCESS);
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
        <div>
            <Topbar/>
            <Menubar/>
            <PageHeader text="Sign up now!"/>

            <div id={styles.errWrapper}>
                <ErrorMsg errMsg={errMsg}/>
            </div>

            <AbstractForm
                initialValues={initialValues}
                onSubmit={handleSubmit}
                fields={[
                    {name: "username", type: "text", label: "Username"},
                    {name: "email", type: "email", label: "E-mail"},
                    {name: "password", type: "password", label: "Password"}
                ]}
                submitButtonText="Sign up!"
                formLink={{to: "/user/login", text: "Already have an account? Sign in!"}}
            />

            <Footer text="Start your trading journey with us!"/>
        </div>
    );
}
