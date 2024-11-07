import React, {useState} from "react";
import Topbar from "../../component/topbar/Topbar";
import {UserRegisterDTO} from "../../dto/user/UserRegisterDTO";
import {registerUser} from "../../util/user/PublicUserService";
import Menubar from "../../component/menubar/Menubar";
import Footer from "../../component/footer/Footer";
import AbstractForm from "../../component/form/AbstractForm";
import PageHeader from "../../component/pageHeader/PageHeader";
import RedirectUtil from "../../util/RedirectUtil";
import styles from "./styles.module.css";
import ErrorMsg from "../../component/error/ErrorMsg";

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
