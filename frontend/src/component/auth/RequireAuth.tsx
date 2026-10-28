import React from "react";
import {Navigate, Outlet} from "react-router-dom";
import {isUserNotAuthenticated, LOGIN_PAGE_URL} from "../../util/AuthUtil.ts";

export default function RequireAuth(): React.ReactElement {
    if (isUserNotAuthenticated()) {
        return <Navigate to={LOGIN_PAGE_URL} replace/>;
    }

    return <Outlet/>;
}
