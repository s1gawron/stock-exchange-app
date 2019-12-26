import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

class StockActions {

    private static boolean containsStock(final List<StockWIG20> list, final String ticker) {
        return list.stream().anyMatch(o -> o.getTicker().equals(ticker));
    }

    private static void printMessage() {
        System.out.println("Transakcja przebiegla pomyslnie.");
    }

    private static void removeStockFromList(final List<StockWIG20> list, final String ticker) {
        list.stream()
                .filter(o -> o.getTicker().equals(ticker) && o.getQuantity() == 0)
                .forEach(list::remove);
    }

    private static void settingParamsOfWallet(User user, int quantity, StockWIG20 stockWIG20, int ratio) {
        user.setStockValue(user.getStockValue() + (ratio * (quantity * stockWIG20.getTempPrice())));
        user.setBalanceAvailable(user.getBalanceAvailable() - (ratio * (quantity * stockWIG20.getTempPrice())));
        user.setWalletValue(user.getStockValue() + user.getBalanceAvailable());
    }

    private static void settingStockQuantity(final List<StockWIG20> list, String ticker, int quantity, int ratio) {
        list.stream()
                .filter(o -> o.getTicker().equals(ticker))
                .forEach(o -> o.setQuantity(o.getQuantity() + (ratio * quantity)));
    }

    private static void timeOfTransaction() {
        Random random = new Random();
        int time = random.nextInt(301);
        try {
            Thread.sleep(time * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static boolean hideActions() {
        Calendar calendar = Calendar.getInstance();
        boolean hideActions;

        if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            hideActions = true;
        } else if (calendar.get(Calendar.HOUR_OF_DAY) < 9 || calendar.get(Calendar.HOUR_OF_DAY) > 17) {
            hideActions = true;
        } else {
            hideActions = false;
        }
        return hideActions;
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

    static void stockPurchase(int quantity, String ticker) {
        User user = User.deserializeUser();
        List<StockWIG20> userStock = new CopyOnWriteArrayList<>(user.getUserStock());
        StockWIG20 stockWIG20 = StockWIG20.getMap().get(ticker);

        if (user.getBalanceAvailable() >= quantity * stockWIG20.getTempPrice() && quantity > 0) {
//            timeOfTransaction();

//            Dodanie akcji do konta uzytkownika, gdy uzytkownik posiada akcje ktore chce kupic:
            if (containsStock(userStock, ticker)) {
                settingStockQuantity(userStock, ticker, quantity, 1);
            } else { // Gdy nie posiada akcji:
                userStock.add(stockWIG20);
                stockWIG20.setQuantity(quantity);
                user.setUserStock(userStock);
            }
            printMessage();
            settingParamsOfWallet(user, quantity, StockWIG20.getMap().get(ticker), 1);
            User.serializeUser(user);
        } else {
            int maxAmount = (int) Math.floor((user.getBalanceAvailable() / (StockWIG20.getMap().get(ticker).getTempPrice())));
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

            if (quantity <= amountOfStock.get() && quantity > 0) {
                settingStockQuantity(userStock, ticker, quantity, -1);
                settingParamsOfWallet(user, quantity, StockWIG20.getMap().get(ticker), -1);
                removeStockFromList(userStock, ticker);
                user.setUserStock(userStock);
                User.serializeUser(user);
                printMessage();
            } else {
                System.out.println("Nie posiadasz takiej ilosci akcji! Ilosc akcji w Twoim portfelu: " + amountOfStock.get());
            }
        } else {
            System.out.println("Nie posiadasz tych akcji!");
        }
    }
}