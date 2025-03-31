import UrlProvider from "../UrlProvider.ts";

export default class TransactionServiceUrlProvider extends UrlProvider {

    private static readonly URL_SUFFIX = "api/transaction";

    getHostSuffix(): string {
        return TransactionServiceUrlProvider.URL_SUFFIX;
    }

    public static v1(): TransactionServiceUrlProvider {
        const instance = new TransactionServiceUrlProvider();
        instance._version = "v1";
        return instance;
    }

    public createTransaction(): TransactionServiceUrlProvider {
        this._path = "create";
        return this;
    }

}