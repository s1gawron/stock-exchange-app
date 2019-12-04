import java.util.Random;

class StockActions {

    static void stockPurchase(int quantity, StockWIG20 stockWIG20) throws InterruptedException {
        if (User.deserializeUser().getBalanceAvailable() >= quantity * stockWIG20.getTempPrice()) {

//            //Czas transakcji
//            Random random = new Random();
//            int time = random.nextInt(301);
//            Thread.sleep(time*1000);

            //Dodanie akcji do konta uzytkownika


            //Ustawienie wartosci akcji na koncie uzytkownika
            User.deserializeUser().setStockValue(quantity * stockWIG20.getTempPrice());

            //Zmiana dostepnychsrodkow
            User.deserializeUser().setBalanceAvailable(User.deserializeUser().getBalanceAvailable() - quantity * stockWIG20.getTempPrice());
        }
    }
}
