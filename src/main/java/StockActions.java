import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

class StockActions {
    private static void timeOfTransaction() {
        Random random = new Random();
        int time = random.nextInt(301);
        try {
            Thread.sleep(time * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static void stockStatus() {
        Calendar calendar = Calendar.getInstance();

        if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            System.out.println("Gielda zamknieta. Zapraszamy w poniedzialek!");
        } else if (calendar.get(Calendar.HOUR_OF_DAY) < 9 || calendar.get(Calendar.HOUR_OF_DAY) > 21) {
            System.out.println("Gielda zamknieta. Wroc o 9!");
        } else {
            System.out.println("Gielda otwarta!");
        }
    }

    private static boolean containsStock(List<StockWIG20> list, String ticker) {
        return list.stream().anyMatch(o -> o.getTicker().equals(ticker));
    }

    private static void removeStockFromList(List<StockWIG20> list, String ticker) {
        list.stream()
                .filter(o -> o.getTicker().equals(ticker) && o.getQuantity() == 0)
                .forEach(list::remove);
    }

    private static void settingParamsOfWallet(User user, int quantity, StockWIG20 stockWIG20, List<StockWIG20> userStock, int ratio) {
//        Ustawienie parametrow przy sprzedazy
        if (ratio == -1) {
            userStock.stream()
                    .filter(o -> o.getTicker().equals(stockWIG20.getTicker()))
                    .forEach(o -> {
                        user.setStockValue(user.getStockValue() + (ratio * (quantity * o.getPrice())));
                        user.setBalanceAvailable(user.getBalanceAvailable() - (ratio * (quantity * o.getPrice())));
                    });
        } else {//Ustawienie parametrow przy kupnie
            user.setStockValue(user.getStockValue() + (ratio * (quantity * stockWIG20.getPrice())));
            user.setBalanceAvailable(user.getBalanceAvailable() - (ratio * (quantity * stockWIG20.getPrice())));
        }
        user.setWalletValue(user.getStockValue() + user.getBalanceAvailable());
    }

    private static void settingStockQuantity(final List<StockWIG20> list, String ticker, int quantity, int ratio) {
        list.stream()
                .filter(o -> o.getTicker().equals(ticker))
                .forEach(o -> o.setQuantity(o.getQuantity() + (ratio * quantity)));
    }

//    Usuwanie sprzedanych akcji z konta uzytkownika
    private static void stockSellParameters(int quantity, int amountOfStock, List<StockWIG20> userStock, User user, String ticker) {
        if (quantity <= amountOfStock && quantity > 0) {
            settingStockQuantity(userStock, ticker, quantity, -1);
            settingParamsOfWallet(user, quantity, StockWIG20.getMap().get(ticker), userStock, -1);
            removeStockFromList(userStock, ticker);
            user.setUserStock(userStock);
            User.serializeUser(user);
            System.out.println("Transakcja przebiegla pomyslnie.");
        } else {
            System.out.println("Nie posiadasz takiej ilosci akcji! Ilosc akcji w Twoim portfelu: " + amountOfStock);
        }
    }

//    Dodanie akcji do konta uzytkownika
    private static void stockPurchaseParameters(int quantity, List<StockWIG20> userStock, String ticker, StockWIG20 stockWIG20, User user) {
//    Gdy uzytkownik posiada akcje ktore chce kupic:
        if (containsStock(userStock, ticker)) {
            settingStockQuantity(userStock, ticker, quantity, 1);
        } else { // Gdy nie posiada akcji:
            userStock.add(stockWIG20);
            stockWIG20.setQuantity(quantity);
            user.setUserStock(userStock);
        }
    }

    static void stockPurchase(int quantity, String ticker) {
        User user = User.deserializeUser();
        List<StockWIG20> userStock = new CopyOnWriteArrayList<>(user.getUserStock());
        StockWIG20 stockWIG20 = StockWIG20.getMap().get(ticker);

        if (user.getBalanceAvailable() >= quantity * stockWIG20.getPrice() && quantity > 0) {
//            timeOfTransaction();
            stockPurchaseParameters(quantity, userStock, ticker, stockWIG20, user);
            System.out.println("Transakcja przebiegla pomyslnie.");
            settingParamsOfWallet(user, quantity, StockWIG20.getMap().get(ticker), userStock, 1);
            User.serializeUser(user);
        } else {
            int maxAmount = (int) Math.floor((user.getBalanceAvailable() / (StockWIG20.getMap().get(ticker).getPrice())));
            System.out.println("Nie masz odpowiednich srodkow, maksymalna ilosc akcji jakie mozesz kupic: " + maxAmount);
        }
    }

    static void stockSell(int quantity, String ticker) {
        User user = User.deserializeUser();
        List<StockWIG20> userStock = new CopyOnWriteArrayList<>(user.getUserStock());

        if (containsStock(userStock, ticker)) {
//            timeOfTransaction();

            Optional<Integer> amountOfStock = userStock.stream()
                    .filter(o -> o.getTicker().equals(ticker))
                    .map(StockWIG20::getQuantity)
                    .findFirst();

            stockSellParameters(quantity, amountOfStock.get(), userStock, user, ticker);
        } else {
            System.out.println("Nie posiadasz tych akcji!");
        }
    }
}