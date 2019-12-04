import java.util.Random;

class StockActions {

    static void stockPurchase(int quantity, String ticker) throws InterruptedException {
        if (User.deserializeUser().getBalanceAvailable() >= quantity * StockWIG20.getMap().get(ticker).getTempPrice()) {

//           Czas transakcji
//           Random random = new Random();
//           int time = random.nextInt(301);
//           Thread.sleep(time*1000);

//           Dodanie akcji do konta uzytkownika


//           Ustawienie wartosci akcji na koncie uzytkownika


//            Zmiana dostepnych srodkow

        }
    }
}
