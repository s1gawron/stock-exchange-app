import UrlProvider from "../../UrlProvider.ts";

export default class UserWalletServiceUrlProvider extends UrlProvider {

    private static readonly URL_SUFFIX: string = "api/user";

    getHostSuffix(): string {
        return UserWalletServiceUrlProvider.URL_SUFFIX;
    }

    public static v2(): UserWalletServiceUrlProvider {
        const instance = new UserWalletServiceUrlProvider();
        instance._version = "v2";
        return instance;
    }

    public wallet(): UserWalletServiceUrlProvider {
        this._path = "wallet";
        return this;
    }

    public stocks(): UserWalletServiceUrlProvider {
        this._path = "stocks";
        return this;
    }
}