import java.util.Random;

class StockActions {

    static void openStock() {

    }

    static void stockPurchase(int quantity, String ticker) {
        User user = User.deserializeUser();

        if (user.getBalanceAvailable() >= quantity * StockWIG20.getMap().get(ticker).getTempPrice()) {


//           Czas transakcji
//           Random random = new Random();
//           int time = random.nextInt(301);
//           Thread.sleep(time*1000);

//           Dodanie akcji do konta uzytkownika


//           Ustawienie wartosci akcji na koncie uzytkownika
            user.setStockValue(user.getStockValue() + (quantity * StockWIG20.getMap().get(ticker).getTempPrice()));

//            Zmiana dostepnych srodkow
            user.setBalanceAvailable(user.getBalanceAvailable() - (quantity * StockWIG20.getMap().get(ticker).getTempPrice()));

//            Zapisanie akcji uzytkownika do konta
            User.serializeUser(user);

        } else {
            System.out.println("Nie masz odpowiednich srodkow mozesz zakupic: ");
        }
    }

    static void stockSell(int quanity, String ticker) {

    }
}
