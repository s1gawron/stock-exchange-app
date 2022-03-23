package pl.eizodev.app.stock.service.exception;

public class StockCompanyDetailsNotFoundException extends RuntimeException {

    private StockCompanyDetailsNotFoundException(final String message) {
        super(message);
    }

    public static StockCompanyDetailsNotFoundException create(final long id) {
        return new StockCompanyDetailsNotFoundException("StockCompanyDetails with id: " + id + " was not found!");
    }

}
