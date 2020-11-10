package pl.eizodev.app.services;

import pl.eizodev.app.entities.Stock;
import pl.eizodev.app.entities.User;

public interface StockService {
    Stock findByUserAndStockTicker(User user, String ticker);
    void saveStock(Stock stock);
    void updateStock(String username);
    void deleteStock(Long id);
}