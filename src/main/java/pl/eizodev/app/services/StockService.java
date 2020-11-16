package pl.eizodev.app.services;

import pl.eizodev.app.entities.Stock;

public interface StockService {
    void saveStock(Stock stock);
    void updateStock(String username);
    void deleteStock(Long id);
}