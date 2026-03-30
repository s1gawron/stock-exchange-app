import React from 'react';
import Topbar from "../../component/topbar/Topbar.tsx";
import Footer from "../../component/footer/Footer.tsx";
import {LoginForm} from "../../component/form/login/LoginForm.tsx";

export default function LogInPage(): React.ReactElement {
    return (
        <>
            <Topbar/>
            <LoginForm/>
            <Footer text="It's good to see you again!"/>
        </>
    );
}
