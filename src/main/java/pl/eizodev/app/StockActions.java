package pl.eizodev.app;

import org.hibernate.Session;
import org.hibernate.Transaction;
import pl.eizodev.app.entity.Stock;
import pl.eizodev.app.entity.User;

import java.util.*;

class StockActions {
    private Session session = HibernateConfig.INSTANCE.getSessionFactory().openSession();
    private Transaction transaction = session.getTransaction();

    private static boolean containsStock(final List<Stock> list, final String ticker) {
        return list.stream().anyMatch(o -> o.getTicker().equals(ticker));
    }

    void stockPurchase(int quantity, String ticker, Long userId) {
        try {
            transaction.begin();
            User user = session.find(User.class, userId);
            List<Stock> userStock = user.getUserStock();
            StockWIG20 stockWIG20 = new StockWIG20();

            if (user.getBalanceAvailable() >= quantity * stockWIG20.getByTicker(ticker).getPrice() && quantity > 0) {
                Optional<Long> stockId = userStock.stream()
                        .filter(o -> o.getTicker().equals(ticker))
                        .map(Stock::getStockId)
                        .findFirst();
                Stock stock = session.find(Stock.class, stockId.get());
                Stock newStock = stockWIG20.getByTicker(ticker);

                if (containsStock(userStock, ticker)) {
                    stock.setAveragePurchasePrice((stock.getQuantity() * stock.getPrice()) + (quantity * stockWIG20.getByTicker(ticker).getPrice()) / (stock.getQuantity() + quantity));
                    stock.setQuantity(stock.getQuantity() + quantity);
                } else {
                    newStock.setQuantity(quantity);
                    newStock.setAveragePurchasePrice(newStock.getPrice());
                    newStock.setUser(user);
                    user.getUserStock().add(newStock);
                    session.save(newStock);
                }
                user.setBalanceAvailable(user.getBalanceAvailable() - (quantity * newStock.getPrice()));
                System.out.println("Transakcja przebiegla pomyslnie.");
            } else {
                int maxAmount = (int) Math.floor((user.getBalanceAvailable() / (stockWIG20.getByTicker(ticker).getPrice())));
                System.out.println("Nie masz odpowiednich srodkow, maksymalna ilosc akcji jakie mozesz kupic: " + maxAmount);
            }
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

    void stockSell(int quantity, String ticker, Long userId) {
        try {
            transaction.begin();
            User user = session.find(User.class, userId);
            List<Stock> userStock = user.getUserStock();
            Optional<Integer> amountOfStock = userStock.stream()
                    .filter(o -> o.getTicker().equals(ticker))
                    .map(Stock::getQuantity)
                    .findFirst();

            if (quantity > 0 && amountOfStock.isPresent()) {
                if (amountOfStock.get() >= quantity) {
                    Optional<Long> stockId = userStock.stream()
                            .filter(o -> o.getTicker().equals(ticker))
                            .map(Stock::getStockId)
                            .findFirst();

                    Stock stock = session.find(Stock.class, stockId.get());
                    stock.setQuantity(stock.getQuantity() - quantity);
                    StockWIG20 stockWIG20 = new StockWIG20();
                    user.setBalanceAvailable(user.getBalanceAvailable() + (quantity * stockWIG20.getByTicker(ticker).getPrice()));

                    if (amountOfStock.get() == quantity) {
                        session.delete(stock);
                    }
                    System.out.println("Transakcja przebiegla pomyslnie.");
                } else {
                    System.out.println("Nie posiadasz takiej ilosci akcji! Ilosc akcji w Twoim portfelu: " + amountOfStock);
                }
            } else {
                System.out.println("Nie posiadasz tych akcji!");
            }
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
    }
}