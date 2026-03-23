const USERNAME = "username";
const TOKEN = "token";

export function isUserAuthenticated(): boolean {
    return sessionStorage.getItem(USERNAME) !== null && sessionStorage.getItem(TOKEN) !== null;
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

export function logIn(username: string, token: string) {
    sessionStorage.setItem(USERNAME, username);
    sessionStorage.setItem(TOKEN, token);
}

export function logOut() {
    sessionStorage.removeItem(USERNAME);
    sessionStorage.removeItem(TOKEN);
}
