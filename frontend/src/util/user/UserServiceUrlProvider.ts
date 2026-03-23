import {buildUrl} from "../UrlProvider.ts";

export function loginUrl(): string {
    return buildUrl("api/public/user", "v1", "login");
}

export function registerUrl(): string {
    return buildUrl("api/public/user", "v1", "register");
}
