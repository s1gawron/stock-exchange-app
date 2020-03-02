package pl.eizodev.app;

import pl.eizodev.app.dao.UserDao;
import pl.eizodev.app.entity.Stock;
import pl.eizodev.app.entity.User;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

class StockActions {
    private static boolean containsStock(final List<Stock> list, final String ticker) {
        return list.stream().anyMatch(o -> o.getTicker().equals(ticker));
    }

    private static void removeStockFromList(final List<Stock> list, final String ticker) {
        list.stream()
                .filter(o -> o.getTicker().equals(ticker) && o.getQuantity() == 0)
                .forEach(list::remove);
    }

    private static void settingStockQuantity(final List<Stock> list, String ticker, int quantity, int ratio) {
        list.stream()
                .filter(o -> o.getTicker().equals(ticker))
                .forEach(o -> o.setQuantity(o.getQuantity() + (ratio * quantity)));
    }

    private static void settingParamsOfWallet(User user, int quantity, Stock stock, int ratio) {
        if (ratio == -1) {
            user.setStockValue(0);
        } else {
            user.setStockValue(user.getStockValue() + (ratio * (quantity * stock.getPrice())));
        }
        user.setBalanceAvailable(user.getBalanceAvailable() - (ratio * (quantity * stock.getPrice())));
        user.setWalletValue(user.getStockValue() + user.getBalanceAvailable());
    }

    static void stockPurchaseParameters(int quantity, List<Stock> userStock, String ticker, Stock stock, User user) {
//        Dodanie akcji do konta uzytkownika, gdy uzytkownik posiada akcje ktore chce kupic:
        if (containsStock(userStock, ticker)) {
            userStock.stream()
                    .filter(o -> o.getTicker().equals(ticker))
                    .forEach(o -> o.setAveragePurchasePrice(((o.getQuantity() * o.getPrice()) + (quantity * stock.getPrice())) / (o.getQuantity() + quantity)));
            settingStockQuantity(userStock, ticker, quantity, 1);
        } else { // Gdy nie posiada akcji:
            userStock.add(stock);
            stock.setQuantity(quantity);
            stock.setAveragePurchasePrice(stock.getPrice());
            user.setUserStock(userStock);
        }
        StockWIG20 stockWIG20 = new StockWIG20();
        settingParamsOfWallet(user, quantity, stockWIG20.getByTicker(ticker), 1);
    }

    //Usuwanie sprzedanych akcji z konta uzytkownika
    static void stockSellParameters(int quantity, int amountOfStock, List<Stock> userStock, User user, String ticker) {
        if (quantity <= amountOfStock && quantity > 0) {
            settingStockQuantity(userStock, ticker, quantity, -1);
            StockWIG20 stockWIG20 = new StockWIG20();
            settingParamsOfWallet(user, quantity, stockWIG20.getByTicker(ticker), -1);
            removeStockFromList(userStock, ticker);
            user.setUserStock(userStock);
//            user.serializeUser(user);
            System.out.println("Transakcja przebiegla pomyslnie.");
        } else {
            System.out.println("Nie posiadasz takiej ilosci akcji! Ilosc akcji w Twoim portfelu: " + amountOfStock);
        }
    }

    void stockPurchase(int quantity, String ticker, Long id) {
        UserDao userDao = new UserDao();
        User user = userDao.getUser(id);
        List<Stock> userStock = new CopyOnWriteArrayList<>(user.getUserStock());
        StockWIG20 stockWIG20 = new StockWIG20();

        if (user.getBalanceAvailable() >= quantity * stockWIG20.getByTicker(ticker).getPrice() && quantity > 0) {
            stockPurchaseParameters(quantity, userStock, ticker, stockWIG20.getByTicker(ticker), user);
            System.out.println("Transakcja przebiegla pomyslnie.");
//            user.serializeUser(finalUser);
        } else {
            int maxAmount = (int) Math.floor((user.getBalanceAvailable() / (stockWIG20.getByTicker(ticker).getPrice())));
            System.out.println("Nie masz odpowiednich srodkow, maksymalna ilosc akcji jakie mozesz kupic: " + maxAmount);
        }
    }

    void stockSell(int quantity, String ticker, Long id) {
        UserDao userDao = new UserDao();
        User user = userDao.getUser(id);
        List<Stock> userStock = new CopyOnWriteArrayList<>(user.getUserStock());

        if (StockActions.containsStock(userStock, ticker)) {

            Optional<Integer> amountOfStock = userStock.stream()
                    .filter(o -> o.getTicker().equals(ticker))
                    .map(Stock::getQuantity)
                    .findFirst();

            stockSellParameters(quantity, amountOfStock.get(), userStock, user, ticker);
        } else {
            System.out.println("Nie posiadasz tych akcji!");
        }
    }
}