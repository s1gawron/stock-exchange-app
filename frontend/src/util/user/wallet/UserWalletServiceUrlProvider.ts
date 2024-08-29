import UrlProvider from "../../UrlProvider";

export default class UserWalletServiceUrlProvider extends UrlProvider {

    private static readonly URL_PREFIX: string = "api/user";

    public static v2(): UserWalletServiceUrlProvider {
        const instance = new UserWalletServiceUrlProvider();
        instance._version = this.URL_PREFIX + "/v2";
        return instance;
    }

    public wallet(): UserWalletServiceUrlProvider {
        this._path = "wallet";
        return this;
    }

}