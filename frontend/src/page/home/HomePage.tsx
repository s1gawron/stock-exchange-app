import React, {useEffect} from "react";
import Footer from "../../component/footer/Footer.tsx";
import {isUserNotAuthenticated} from "../../util/AuthUtil.ts";
import {useNavigate} from "react-router-dom";
import PageHeader from "../../component/pageHeader/PageHeader.tsx";
import Topbar from "../../component/topbar/Topbar.tsx";

const REDIRECT_TO_LOGIN_PAGE: string = "/user/login";

export default function HomePage(): React.ReactElement {
    const navigate = useNavigate();

    useEffect(() => {
        if (isUserNotAuthenticated()) {
            navigate(REDIRECT_TO_LOGIN_PAGE);
            return;
        }
    }, [navigate]);

    return (
        <>
            <Topbar/>
            <PageHeader text="Stock Exchange Simulator"/>
            <Footer text="Thank you for trading with us! Here's to your success in the market!"/>
        </>
    );
}
