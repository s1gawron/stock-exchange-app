import React, {useState} from 'react';
import Topbar from "../../component/topbar/Topbar";
import Menubar from "../../component/menubar/Menubar";
import {loginUser} from "../../util/PublicUserRestService";
import {UserLoginDTO} from "../../dto/UserLoginDTO";
import Footer from "../../component/footer/Footer";
import AbstractForm from "../../component/form/AbstractForm";

export default function LoginPage(): React.ReactElement {
    const initialValues: UserLoginDTO = {
        username: "",
        password: ""
    };
    const [errMsg, setErrMsg] = useState<string>("");

    const handleSubmit = (values: UserLoginDTO) => {
        loginUser(values, setErrMsg);
    };

    return (
        <div>
            <Topbar/>
            <Menubar/>

            <div id="registerHeaderContainer">
                <h3>Sign in!</h3>
            </div>

            <AbstractForm
                initialValues={initialValues}
                onSubmit={handleSubmit}
                fields={[
                    {name: 'username', type: 'text', label: 'Username'},
                    {name: 'password', type: 'password', label: 'Password'},
                ]}
                submitButtonText="Sign in!"
                errorMessage={errMsg}
                formLink={{to: "/user/register", text: "First time? Sign up!"}}
            />

            <Footer text="It's good to see you again!"/>
        </div>
    );
}
