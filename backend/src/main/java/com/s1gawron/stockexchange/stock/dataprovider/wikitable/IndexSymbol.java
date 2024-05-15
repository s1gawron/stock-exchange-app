package com.s1gawron.stockexchange.stock.dataprovider.wikitable;

public enum IndexSymbol {

    SP500("/List_of_S&P_500_companies", 0),
    DJI("/Dow_Jones_Industrial_Average", 0),
    NASDAQ100("/Nasdaq-100", 3);

    private final String urlPath;

    private final int tableNumber;

    IndexSymbol(final String urlPath, final int tableNumber) {
        this.urlPath = urlPath;
        this.tableNumber = tableNumber;
    }

    public String getUrlPath() {
        return urlPath;
    }

    public int getTableNumber() {
        return tableNumber;
    }
}
