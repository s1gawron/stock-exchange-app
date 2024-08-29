import UrlProvider from "../UrlProvider";

export default class StockListingsUrlProvider extends UrlProvider {

    private static readonly URL_SUFFIX: string = "api/public/stock";

    getHostSuffix(): string {
        return StockListingsUrlProvider.URL_SUFFIX;
    }

    public static v2(): StockListingsUrlProvider {
        const instance = new StockListingsUrlProvider();
        instance._version = "v2";
        return instance;
    }

    public index(indexVal: string): StockListingsUrlProvider {
        this._path = `index/${indexVal}`;
        return this;
    }

}