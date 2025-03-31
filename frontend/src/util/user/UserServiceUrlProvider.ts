import UrlProvider from "../UrlProvider.ts";

export default class UserServiceUrlProvider extends UrlProvider {

    private static readonly URL_SUFFIX: string = "api/public/user";

    getHostSuffix(): string {
        return UserServiceUrlProvider.URL_SUFFIX;
    }

    public static v1(): UserServiceUrlProvider {
        const instance = new UserServiceUrlProvider();
        instance._version = "v1";
        return instance;
    }

    public static v2(): UserServiceUrlProvider {
        const instance = new UserServiceUrlProvider();
        instance._version = "v2";
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