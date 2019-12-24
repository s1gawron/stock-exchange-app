import lombok.ToString;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

class StockActions {

    private static boolean containsName(final List<StockWIG20> list, final String name) {
        return list.stream().anyMatch(o -> o.getName().equals(name));
    }

    private static void openStock() {
        Calendar calendar = Calendar.getInstance();

        if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            System.out.println("Gielda zamknieta. Zapraszamy w poniedzialek!");
            System.exit(0);
        } else if (calendar.get(Calendar.HOUR_OF_DAY) < 9 || calendar.get(Calendar.HOUR_OF_DAY) > 17) {
            System.out.println("Gielda zamknieta. Wroc o 9!");
            System.exit(0);
        } else {
            System.out.println("Gielda otwarta!");
        }
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

    static void stockPurchase(int quantity, String ticker) {
        User user = User.deserializeUser();
        List<StockWIG20> userStock = new CopyOnWriteArrayList<>(user.getUserStock());
        StockWIG20 stockWIG20 = StockWIG20.getMap().get(ticker);

        openStock();

        if (user.getBalanceAvailable() >= quantity * StockWIG20.getMap().get(ticker).getTempPrice()) {
            timeOfTransaction();

//            Dodanie akcji do konta uzytkownika, gdy uzytkownik posiada akcje ktore chce kupic:
            if (containsName(userStock, stockWIG20.getName())) {
                userStock.stream()
                        .filter(o -> o.getTicker().equals(ticker))
                        .forEach(o -> o.setQuantity(o.getQuantity() + quantity));
            } else { // Gdy nie posiada akcji:
                userStock.add(stockWIG20);
                stockWIG20.setQuantity(quantity);
                user.setUserStock(userStock);
            }

            user.setStockValue(user.getStockValue() + (quantity * StockWIG20.getMap().get(ticker).getTempPrice()));
            user.setBalanceAvailable(user.getBalanceAvailable() - (quantity * StockWIG20.getMap().get(ticker).getTempPrice()));
            User.serializeUser(user);

        } else {
            int maxAmount = (int) Math.floor((user.getBalanceAvailable() / (StockWIG20.getMap().get(ticker).getTempPrice())));
            System.out.println("Nie masz odpowiednich srodkow, mozesz zakupic: " + maxAmount);
        }
    }

    static void stockSell(int quanity, String ticker) {
        User user = User.deserializeUser();
        List<StockWIG20> userStock = new CopyOnWriteArrayList<>(user.getUserStock());
        StockWIG20 stockWIG20 = StockWIG20.getMap().get(ticker);

        openStock();

        if (containsName(userStock, stockWIG20.getName())) {
            Optional<Integer> amountOfStock = userStock.stream()
                    .filter(o -> o.getTicker().equals(ticker))
                    .map(StockWIG20::getQuantity)
                    .findFirst();

//            if (quanity <= amountOfStock.get()) {
//
//            }

        } else {
            System.out.println("Nie posiadasz tych akcji!");
        }
    }
}