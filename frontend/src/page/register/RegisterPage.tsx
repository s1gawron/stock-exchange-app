import React, {useState} from "react";
import Topbar from "../../component/topbar/Topbar";
import {UserRegisterDTO} from "../../dto/user/UserRegisterDTO";
import PublicUserService from "../../util/user/PublicUserService";
import Menubar from "../../component/menubar/Menubar";
import Footer from "../../component/footer/Footer";
import AbstractForm from "../../component/form/AbstractForm";
import PageHeader from "../../component/pageHeader/PageHeader";

const USER_WALLET_BALANCE_DEF_VAL: number = 10_000;

export default function RegisterPage(): React.ReactElement {
    const initialValues: UserRegisterDTO = {
        username: "",
        email: "",
        password: "",
        userWalletBalance: USER_WALLET_BALANCE_DEF_VAL,
    };
    const [errMsg, setErrMsg] = useState<string>("");

    const handleSubmit = (values: UserRegisterDTO) => {
        PublicUserService.registerUser(values, setErrMsg);
    };

    return (
        <div>
            <Topbar/>
            <Menubar/>
            <PageHeader text="Sign up now!"/>

            <AbstractForm
                initialValues={initialValues}
                onSubmit={handleSubmit}
                fields={[
                    {name: "username", type: "text", label: "Username"},
                    {name: "email", type: "email", label: "E-mail"},
                    {name: "password", type: "password", label: "Password"},
                    {
                        name: "userWalletBalance",
                        type: "select",
                        label: "Initial capital",
                        options: [
                            {value: 5000, label: "5000"},
                            {value: 10000, label: "10000"},
                            {value: 20000, label: "20000"},
                        ],
                    },
                ]}
                submitButtonText="Sign up!"
                errorMessage={errMsg}
                formLink={{to: "/user/login", text: "Already have an account? Sign in!"}}
            />

            <Footer text="Start your trading journey with us!"/>
        </div>
    );
}
