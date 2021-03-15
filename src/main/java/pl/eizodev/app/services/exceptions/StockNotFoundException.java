package pl.eizodev.app.services.exceptions;

public class StockNotFoundException extends RuntimeException {

    private StockNotFoundException(final String message) {
        super(message);
    }

    public static StockNotFoundException create(final String ticker) {
        return new StockNotFoundException("Stock with ticker " + ticker + " could not be found!");
    }
}
