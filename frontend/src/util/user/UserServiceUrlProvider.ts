import UrlProvider from "../UrlProvider";

export default class UserServiceUrlProvider extends UrlProvider {

    private static readonly URL_PREFIX: string = "api/public/user";

    public static create(): UserServiceUrlProvider {
        return new UserServiceUrlProvider();
    }

    public static v1(): UserServiceUrlProvider {
        const instance = new UserServiceUrlProvider();
        instance._version = this.URL_PREFIX + "/v1";
        return instance;
    }

    public static v2(): UserServiceUrlProvider {
        const instance = new UserServiceUrlProvider();
        instance._version = this.URL_PREFIX + "/v2";
        return instance;
    }

    public login(): UserServiceUrlProvider {
        this._path = "login";
        return this;
    }

    public register(): UserServiceUrlProvider {
        this._path = "register";
        return this;
    }
}