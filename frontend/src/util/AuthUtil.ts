const USERNAME = "username";
const TOKEN = "token";
const TOKEN_EXPIRATION_DATE = "tokenExpirationDate";

export const LOGIN_PAGE_URL: string = "/user/login";

export function isUserAuthenticated(): boolean {
    const expirationDate = sessionStorage.getItem(TOKEN_EXPIRATION_DATE);
    return sessionStorage.getItem(USERNAME) !== null && sessionStorage.getItem(TOKEN) !== null && expirationDate !== null
        && Date.parse(expirationDate) > Date.now();
}

export function isUserNotAuthenticated(): boolean {
    return !isUserAuthenticated();
}

export function getToken(): string | null {
    return sessionStorage.getItem(TOKEN);
}

export function getUsername(): string | null {
    return sessionStorage.getItem(USERNAME);
}

export function logIn(username: string, token: string, tokenExpirationDate: string) {
    sessionStorage.setItem(USERNAME, username);
    sessionStorage.setItem(TOKEN, token);
    sessionStorage.setItem(TOKEN_EXPIRATION_DATE, tokenExpirationDate);
}

export function logOut() {
    sessionStorage.removeItem(USERNAME);
    sessionStorage.removeItem(TOKEN);
    sessionStorage.removeItem(TOKEN_EXPIRATION_DATE);
}
