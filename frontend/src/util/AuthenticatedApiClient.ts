import axios, {AxiosError, HttpStatusCode} from "axios";
import {getToken, logOut} from "./AuthUtil.ts";

const REDIRECT_TO_LOGIN_PAGE: string = "/user/login";

const authenticatedApiClient = axios.create();

authenticatedApiClient.interceptors.request.use(config => {
    const jwt = getToken();

    if (jwt !== null) {
        config.headers.Authorization = `Bearer ${jwt}`;
    }

    return config;
});

authenticatedApiClient.interceptors.response.use(res => res, (error: unknown) => {
    const status = error instanceof AxiosError ? error.response?.status : undefined;

    // Missing, expired or invalid JWT is rejected by Spring Security with 401/403
    if (status === HttpStatusCode.Unauthorized || status === HttpStatusCode.Forbidden) {
        logOut();
        window.location.assign(REDIRECT_TO_LOGIN_PAGE);
    }

    return Promise.reject(error);
});

export default authenticatedApiClient;
