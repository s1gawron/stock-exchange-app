import UrlProvider from "../UrlProvider.ts";

export default class StockServiceUrlProvider extends UrlProvider {

    private static readonly URL_SUFFIX: string = "api/public/stock";

    getHostSuffix(): string {
        return StockServiceUrlProvider.URL_SUFFIX;
    }

    public static v2(): StockServiceUrlProvider {
        const instance = new StockServiceUrlProvider();
        instance._version = "v2";
        return instance;
    }

    public index(indexVal: string): StockServiceUrlProvider {
        this._path = `index/${indexVal}`;
        return this;
    }

    public stockData(ticker: string): StockServiceUrlProvider {
        this._path = ticker;
        return this;
    }

}