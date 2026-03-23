import React, {useState} from "react";
import Topbar from "../../component/topbar/Topbar.tsx";
import {UserRegisterDTO} from "../../dto/user/UserRegisterDTO.ts";
import {registerUser} from "../../util/user/PublicUserService.ts";
import Menubar from "../../component/menubar/Menubar.tsx";
import Footer from "../../component/footer/Footer.tsx";
import RegisterForm from "../../component/form/RegisterForm.tsx";
import PageHeader from "../../component/pageHeader/PageHeader.tsx";
import {useNavigate} from "react-router-dom";
import styles from "./styles.module.css";
import ErrorMsg from "../../component/error/ErrorMsg.tsx";

const REDIRECT_URL_AFTER_SIGN_UP_SUCCESS: string = "/user/login?fromSignUp=true";

export default function RegisterPage(): React.ReactElement {
    const [errMsg, setErrMsg] = useState<string>("");
    const navigate = useNavigate();

    const handleSubmit = (values: UserRegisterDTO) => {
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
        <div>
            <Topbar/>
            <Menubar/>
            <PageHeader text="Sign up now!"/>

            <div id={styles.errWrapper}>
                <ErrorMsg errMsg={errMsg}/>
            </div>

            <RegisterForm onSubmit={handleSubmit}/>

            <Footer text="Start your trading journey with us!"/>
        </div>
    );
}
