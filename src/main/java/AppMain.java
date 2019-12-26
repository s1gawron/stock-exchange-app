import java.util.Calendar;
import java.util.Scanner;

public class AppMain {
    private static Scanner scanner = new Scanner(System.in);

    private static void getUserDecision (String choiceYN, int action) {
        String stockChoice;
        int quantity;
        if ((action == 0) && (choiceYN.equals("Y") || choiceYN.equals("y"))) {
            System.out.println("Podaj symbol spolki ktora chcesz sprzedac: ");
            stockChoice = scanner.nextLine();
            System.out.println("Podaj ilosc akcji ktore chcesz sprzedac: ");
            quantity = scanner.nextInt();
            StockActions.stockSell(quantity, stockChoice);
            System.out.println("Transakcja przebiegla pomyslnie.");
        } else if ((action == 1) && (choiceYN.equals("Y") || choiceYN.equals("y"))) {
            System.out.println("Podaj symbol spolki ktora chcesz kupic: ");
            stockChoice = scanner.nextLine();
            System.out.println("Podaj ilosc akcji ktore chcesz kupic: ");
            quantity = scanner.nextInt();
            StockActions.stockPurchase(quantity, stockChoice);
            System.out.println("Transakcja przebiegla pomyslnie.");
        }
    }

    public static void main(String[] args) {
        User user = User.deserializeUser();
        int choice;

        System.out.println("Dzien dobry " + user.getName() + "!");
        System.out.println("Dzisiaj jest: " + Calendar.getInstance().getTime());
        StockActions.openStock();

        do {
            user = User.deserializeUser();
            System.out.println("\nCo chcesz zrobic?");
            System.out.println("1. Moj portfel");
            System.out.println("2. Gielda (WIG20)");
            System.out.println("3. Wyjscie");
            System.out.print("Wprowadz swoj wybor: ");
            choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    System.out.flush();
                    System.out.println("\nKonto: " + user.getName());
                    System.out.println("Dostepne saldo: " + user.getBalanceAvailable());
                    System.out.println("Wartosc posiadanych akcji przez Ciebie akcji wynosi: " + user.getStockValue());
                    System.out.println("Wartosc Twojego portfela wynosi: " + user.getWalletValue());
                    System.out.println("Poprzednia wartosc Twojego portfela wynosila: " + user.getPrevWalletValue());
                    System.out.println("Procentowa zmiana wartosci portfela: ");

                    if (user.getUserStock().isEmpty()){
                        System.out.println("Nie posiadasz zadnych akcji.");
                    } else {
                        System.out.println("Akcje ktore posiadasz: " + user.getUserStock());
                        System.out.println("\nCzy chcialbys sprzedac jakies akcje? (Y/N):");
                        String sellChoice = scanner.nextLine();
                        getUserDecision(sellChoice, 0);
                    }
                    break;
                case 2:
                    System.out.println(StockWIG20.getMap());
                    System.out.println("\nCzy chcialbys kupic jakies akcje? (Y/N):");
                    String purchaseChoice = scanner.nextLine();
                    getUserDecision(purchaseChoice, 1);
                    break;
                case 3:
                    break;
                default:
                    System.out.println("\nNie istnieje taka opcja");
                    break;
            }
        } while (choice != 3);
        System.out.println("\nDo zobaczenia!");
    }
}