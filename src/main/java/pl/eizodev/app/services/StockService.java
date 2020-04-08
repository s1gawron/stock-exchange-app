package pl.eizodev.app.services;

import pl.eizodev.app.entity.Stock;

public interface StockService {
    void saveStock(Stock stock);
    void deleteStock(Long id);
}
