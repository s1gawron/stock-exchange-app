package pl.eizodev.app.services;

import pl.eizodev.app.entity.Stock;
import pl.eizodev.app.entity.User;

public interface StockService {
    Stock findById(Long id);
    Stock findByUserAndStockTicker(User user, String ticker);
    void saveStock(Stock stock);
    void updateStock(User user);
    void deleteStock(Long id);
}