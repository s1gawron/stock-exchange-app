package pl.eizodev.app;

import org.hibernate.Session;
import pl.eizodev.app.dao.UserDao;
import pl.eizodev.app.entity.Stock;
import pl.eizodev.app.entity.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class MainMenu {
    private static Scanner scanner = new Scanner(System.in);
    private static LocalDateTime localDateTime = LocalDateTime.now();

    private static void stockStatus() {
        if (localDateTime.getDayOfWeek().getValue() == 6 || localDateTime.getDayOfWeek().getValue() == 7) {
            System.out.println("Gielda zamknieta. Zapraszamy w poniedzialek!");
        } else if (localDateTime.getHour() < 9 || localDateTime.getHour() >= 17) {
            System.out.println("Gielda zamknieta. Wroc o 9!");
        } else {
            System.out.println("Gielda otwarta!");
        }
    }

    private static void getUserDecision(String choiceYN, int action, Long userId) {
        StockActions stockActions = new StockActions();
        String stockChoice;
        int quantity;
        if ((action == 0) && (choiceYN.equals("Y") || choiceYN.equals("y"))) {
            System.out.println("Podaj symbol spolki ktora chcesz sprzedac: ");
            stockChoice = scanner.nextLine();
            System.out.println("Podaj ilosc akcji ktore chcesz sprzedac: ");
            quantity = scanner.nextInt();
            stockActions.stockSell(quantity, stockChoice, userId);
        } else if ((action == 1) && (choiceYN.equals("Y") || choiceYN.equals("y"))) {
            System.out.println("Podaj symbol spolki ktora chcesz kupic: ");
            stockChoice = scanner.nextLine();
            System.out.println("Podaj ilosc akcji ktore chcesz kupic: ");
            quantity = scanner.nextInt();
            stockActions.stockPurchase(quantity, stockChoice, userId);
        }
    }

    private static boolean hiddenActions() {
        boolean showActions;

        if (localDateTime.getDayOfWeek().getValue() == 6 || localDateTime.getDayOfWeek().getValue() == 7) {
            showActions = false;
        } else if (localDateTime.getHour() < 9 || localDateTime.getHour() >= 17) {
            showActions = false;
        } else {
            showActions = true;
        }
        return showActions;
    }

    void menu(Long userId) {
        Session session = HibernateConfig.INSTANCE.getSessionFactory().openSession();
        User user = session.get(User.class, userId);

        int choice;
        float walletPercChange = (user.getWalletValue() - user.getPrevWalletValue()) / user.getPrevWalletValue();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy HH:mm:ss");

        System.out.println("Dzien dobry " + user.getName() + "!");
        System.out.println("Dzisiaj jest: " + localDateTime.format(formatter));
        stockStatus();

        do {
            System.out.println("\nCo chcesz zrobic?");
            System.out.println("1. Moj portfel");
            System.out.println("2. Gielda (WIG20)");
            System.out.println("3. Wyjscie");
            System.out.print("Wprowadz swoj wybor: ");
            choice = scanner.nextInt();
            scanner.nextLine();

            if (choice == 1) {
                UserDao userDao = new UserDao();
                userDao.updateUser(userId);
                User updatedUser = session.get(User.class, userId);
                System.out.println("\nKonto: " + updatedUser.getName());
                System.out.println("Dostepne saldo: " + updatedUser.getBalanceAvailable());
                System.out.println("Wartosc posiadanych akcji przez Ciebie akcji wynosi: " + updatedUser.getStockValue());
                System.out.println("Wartosc Twojego portfela wynosi: " + updatedUser.getWalletValue());
                System.out.println("Poprzednia wartosc Twojego portfela wynosila: " + updatedUser.getPrevWalletValue());
                System.out.println("Procentowa zmiana wartosci portfela: " + walletPercChange + "%");

                if (user.getUserStock().isEmpty()) {
                    System.out.println("Nie posiadasz zadnych akcji.");
                } else {
                    System.out.println("Akcje ktore posiadasz: " + updatedUser.getUserStock());
                    if (hiddenActions()) {
                        System.out.println("\nCzy chcialbys sprzedac jakies akcje? (Y/N):");
                        String sellChoice = scanner.nextLine();
                        getUserDecision(sellChoice, 0, userId);
                    }
                }
            } else if (choice == 2) {
                StockWIG20 stockWIG20 = new StockWIG20();
                for (Stock stock: stockWIG20.getAll()) {
                    System.out.println(stock);
                }
                if (hiddenActions()) {
                    System.out.println("\nCzy chcialbys kupic jakies akcje? (Y/N):");
                    String purchaseChoice = scanner.nextLine();
                    getUserDecision(purchaseChoice, 1, userId);
                }
            } else if (choice == 3) {
                System.out.println("\nDo zobaczenia!");
            } else {
                System.out.println("\nNie istnieje taka opcja!");
            }
        } while (choice != 3);
        session.close();
    }
}