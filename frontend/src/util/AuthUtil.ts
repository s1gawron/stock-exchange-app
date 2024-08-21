export default class AuthUtil {

    private static readonly USERNAME = "username";

    private static readonly TOKEN = "token";

    static isUserAuthenticated(): boolean {
        return sessionStorage.getItem(this.USERNAME) !== null && sessionStorage.getItem(this.TOKEN) !== null;
    }

    static isUserNotAuthenticated(): boolean {
        return !this.isUserAuthenticated();
    }

    static getToken(): string | null {
        return sessionStorage.getItem(this.TOKEN);
    }

    static getUsername(): string | null {
        return sessionStorage.getItem(this.USERNAME);
    }

    static logIn(username: string, token: string) {
        sessionStorage.setItem(this.USERNAME, username);
        sessionStorage.setItem(this.TOKEN, token);
    }

    static logOut() {
        sessionStorage.removeItem(this.USERNAME);
        sessionStorage.removeItem(this.TOKEN);
    }

}