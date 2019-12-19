import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class StockActions {

//    static boolean openStock() {
//
//    }

    static void stockPurchase(int quantity, String ticker) {
        User user = User.deserializeUser();

        if (user.getBalanceAvailable() >= quantity * StockWIG20.getMap().get(ticker).getTempPrice()) {

//           Czas transakcji
//           Random random = new Random();
//           int time = random.nextInt(301);
//           Thread.sleep(time*1000);

//           Dodanie akcji do konta uzytkownika
            List<StockWIG20> userStock = new ArrayList<>(user.getUserStock());
            StockWIG20 stockWIG20 = StockWIG20.getMap().get(ticker);

            if (user.getUserStock().contains(stockWIG20)) {
                //stockWIG20.setQuantity(stockWIG20.getQuantity() + quantity);
                System.out.println("Jest tu !!!!");
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
