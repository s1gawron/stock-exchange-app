import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Scanner;

public class MainMenu {
    private static Scanner scanner = new Scanner(System.in);

    private static void stockStatus() {
        Calendar calendar = Calendar.getInstance();

        if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            System.out.println("Gielda zamknieta. Zapraszamy w poniedzialek!");
        } else if (calendar.get(Calendar.HOUR_OF_DAY) < 9 || calendar.get(Calendar.HOUR_OF_DAY) > 21) {
            System.out.println("Gielda zamknieta. Wroc o 9!");
        } else {
            System.out.println("Gielda otwarta!");
        }
    }

    private static void getUserDecision(String choiceYN, int action) {
        String stockChoice;
        int quantity;
        if ((action == 0) && (choiceYN.equals("Y") || choiceYN.equals("y"))) {
            System.out.println("Podaj symbol spolki ktora chcesz sprzedac: ");
            stockChoice = scanner.nextLine();
            System.out.println("Podaj ilosc akcji ktore chcesz sprzedac: ");
            quantity = scanner.nextInt();
            StockActions.stockSell(quantity, stockChoice);
        } else if ((action == 1) && (choiceYN.equals("Y") || choiceYN.equals("y"))) {
            System.out.println("Podaj symbol spolki ktora chcesz kupic: ");
            stockChoice = scanner.nextLine();
            System.out.println("Podaj ilosc akcji ktore chcesz kupic: ");
            quantity = scanner.nextInt();
            StockActions.stockPurchase(quantity, stockChoice);
        }
    }

    private static boolean hiddenActions() {
        Calendar calendar = Calendar.getInstance();
        boolean showActions;

        if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            showActions = false;
        } else if (calendar.get(Calendar.HOUR_OF_DAY) < 9 || calendar.get(Calendar.HOUR_OF_DAY) > 17) {
            showActions = false;
        } else {
            showActions = true;
        }
        return showActions;
    }

    static void mainMenu() {
        User user = User.deserializeUser();
        int choice;
        float walletPercChange = (user.getWalletValue() - user.getPrevWalletValue()) / user.getPrevWalletValue();
        LocalDateTime localDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy HH:mm:ss");

        System.out.println("Dzien dobry " + user.getName() + "!");
        System.out.println("Dzisiaj jest: " + localDateTime.format(formatter));
        stockStatus();

        do {
            user = User.deserializeUser();
            System.out.println("\nCo chcesz zrobic?");
            System.out.println("1. Moj portfel");
            System.out.println("2. Gielda (WIG20)");
            System.out.println("3. Wyjscie");
            System.out.print("Wprowadz swoj wybor: ");
            choice = scanner.nextInt();
            scanner.nextLine();

            if (choice == 1) {
                User.userUpdate();
                System.out.println("\nKonto: " + user.getName());
                System.out.println("Dostepne saldo: " + user.getBalanceAvailable());
                System.out.println("Wartosc posiadanych akcji przez Ciebie akcji wynosi: " + user.getStockValue());
                System.out.println("Wartosc Twojego portfela wynosi: " + user.getWalletValue());
                System.out.println("Poprzednia wartosc Twojego portfela wynosila: " + user.getPrevWalletValue());
                System.out.println("Procentowa zmiana wartosci portfela: " + walletPercChange + "%");

                if (user.getUserStock().isEmpty()) {
                    System.out.println("Nie posiadasz zadnych akcji.");
                } else {
                    System.out.println("Akcje ktore posiadasz: " + user.getUserStock());
                    if (hiddenActions()) {
                        System.out.println("\nCzy chcialbys sprzedac jakies akcje? (Y/N):");
                        String sellChoice = scanner.nextLine();
                        getUserDecision(sellChoice, 0);
                    }
                }
            } else if (choice == 2) {
                System.out.println("\n" + StockWIG20.getMap());
                if (hiddenActions()) {
                    System.out.println("\nCzy chcialbys kupic jakies akcje? (Y/N):");
                    String purchaseChoice = scanner.nextLine();
                    getUserDecision(purchaseChoice, 1);
                }
            } else if (choice == 3) {
                System.out.println("\nDo zobaczenia!");
            } else {
                System.out.println("\nNie istnieje taka opcja!");
            }
        } while (choice != 3);
    }
}