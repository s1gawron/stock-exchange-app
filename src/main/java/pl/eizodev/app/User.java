package pl.eizodev.app;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.*;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
class User {
    private String name;
    private String userUpdate;
    private float stockValue;
    private float balanceAvailable;
    private float walletValue;
    private float prevWalletValue;
    private List<Stock> userStock;

    static User deserializeUser() {
        File jsonFile = new File("user.json");
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(jsonFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return new Gson().fromJson(Objects.requireNonNull(fileReader), User.class);
    }

    static void serializeUser(User user) {
        Gson gson = new GsonBuilder().serializeNulls().create();
        String serializedUser = gson.toJson(user);
        try {
            FileWriter save = new FileWriter("user.json");
            save.write(serializedUser);
            save.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void userUpdate() {
        User user = User.deserializeUser();
        List<Stock> userStock = user.getUserStock();

        LocalDateTime localDate = LocalDateTime.now();
        String lastUpdateString = user.getUserUpdate();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss");
        String dateString = localDate.format(formatter);
        LocalDate lastUpdate = LocalDate.parse(lastUpdateString, formatter);

        userStock
                .forEach(o -> user.setStockValue(o.getQuantity() * o.getPrice()));

        user.setWalletValue(user.getStockValue() + user.getBalanceAvailable());

        if (localDate.getDayOfMonth() > lastUpdate.getDayOfMonth()) {
            user.setPrevWalletValue(user.getWalletValue());
        }

        user.setUserUpdate(dateString);

        StockWIG20Api stock = new StockWIG20Api();

        userStock.stream()
                .filter(o -> o.getTicker().equals(stock.getByTicker(o.getTicker()).getTicker()))
                .forEach(o -> o.setPrice(stock.getByTicker(o.getTicker()).getPrice()));

        User.serializeUser(user);
    }
}