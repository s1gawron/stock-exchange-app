import React from "react";
import Topbar from "../../component/topbar/Topbar.tsx";
import Footer from "../../component/footer/Footer.tsx";
import RegisterForm from "../../component/form/register/RegisterForm.tsx";

export default function RegisterPage(): React.ReactElement {
    return (
        <div>
            <Topbar/>
            <RegisterForm/>
            <Footer text="Start your trading journey with us!"/>
        </div>
    );
}
