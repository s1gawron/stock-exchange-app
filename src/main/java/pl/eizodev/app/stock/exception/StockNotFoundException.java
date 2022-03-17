package pl.eizodev.app.stock.exception;

public class StockNotFoundException extends RuntimeException {

    private StockNotFoundException(final String message) {
        super(message);
    }

    public static StockNotFoundException createFromTicker(final String ticker) {
        return new StockNotFoundException("Stock with ticker " + ticker + " could not be found!");
    }

    public static StockNotFoundException createFromQuery(final String query) {
        return new StockNotFoundException("Could not match any stock with given query: " + query);
    }
}
