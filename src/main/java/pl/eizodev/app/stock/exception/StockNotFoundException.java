package pl.eizodev.app.stock.exception;

public class StockNotFoundException extends RuntimeException {

    private StockNotFoundException(final String message) {
        super(message);
    }

    public static StockNotFoundException create(final String ticker) {
        return new StockNotFoundException("Stock with ticker " + ticker + " could not be found!");
    }
}
