import UrlProvider from "../UrlProvider";

export default class StockListingsUrlProvider extends UrlProvider {

    private static readonly URL_PREFIX: string = "api/public/stock";

    public static v2(): StockListingsUrlProvider {
        const instance = new StockListingsUrlProvider();
        instance._version = this.URL_PREFIX + "/v2";
        return instance;
    }

    public index(indexVal: string): StockListingsUrlProvider {
        this._path = `index/${indexVal}`;
        return this;
    }

}