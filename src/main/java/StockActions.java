import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

class StockActions {

    private static boolean containsName(final List<StockWIG20> list, final String name) {
        return list.stream().anyMatch(o -> o.getName().equals(name));
    }

    static void openStock() {
        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("EEE HH:mm");

        if (dateFormat.format(date).contains("czw.")) {
            System.out.println("Gielda czynna!");
        }
    }

    static void stockPurchase(int quantity, String ticker) {
        User user = User.deserializeUser();

        if (user.getBalanceAvailable() >= quantity * StockWIG20.getMap().get(ticker).getTempPrice()) {

//           Czas transakcji
//           Random random = new Random();
//           int time = random.nextInt(301);
//           Thread.sleep(time*1000);

//           Dodanie akcji do konta uzytkownika
            List<StockWIG20> userStock = new CopyOnWriteArrayList<>(user.getUserStock());
            StockWIG20 stockWIG20 = StockWIG20.getMap().get(ticker);

            if (containsName(userStock, stockWIG20.getName())) {
                userStock.stream()
                        .filter(o -> o.getTicker().equals(ticker))
                        .forEach(o -> o.setQuantity(o.getQuantity() + quantity));
            } else {
                userStock.add(stockWIG20);
                stockWIG20.setQuantity(quantity);
            }
            user.setUserStock(userStock);


//           Ustawienie wartosci akcji na koncie uzytkownika
            user.setStockValue(user.getStockValue() + (quantity * StockWIG20.getMap().get(ticker).getTempPrice()));

//            Zmiana dostepnych srodkow
            user.setBalanceAvailable(user.getBalanceAvailable() - (quantity * StockWIG20.getMap().get(ticker).getTempPrice()));

//            Zapisanie akcji uzytkownika do konta
            User.serializeUser(user);

        } else {
            System.out.println("Nie masz odpowiednich srodkow, mozesz zakupic: ");
        }
    }

    static void stockSell(int quanity, String ticker) {

    }
}