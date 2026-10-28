const USERNAME = "username";
const TOKEN = "token";

export const LOGIN_PAGE_URL: string = "/user/login";

export function isUserAuthenticated(): boolean {
    const token = sessionStorage.getItem(TOKEN);
    return sessionStorage.getItem(USERNAME) !== null && token !== null && !isTokenExpired(token);
}

function isTokenExpired(token: string): boolean {
    try {
        const payload = token.split(".")[1].replace(/-/g, "+").replace(/_/g, "/");
        const {exp} = JSON.parse(atob(payload));
        return typeof exp !== "number" || exp * 1000 <= Date.now();
    } catch {
        return true;
    }
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
