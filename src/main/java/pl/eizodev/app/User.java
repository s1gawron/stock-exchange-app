package pl.eizodev.app;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.*;
import org.hibernate.annotations.CollectionId;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Table(name = "users")
class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long userId;
    private String name;
    private LocalDate userUpdate;
    private float stockValue;
    private float balanceAvailable;
    private float walletValue;
    private float prevWalletValue;
    @ElementCollection
    @JoinTable(
            name = "users_stock",
            joinColumns = @JoinColumn(name = "USER_ID")
    )
    @GenericGenerator(name = "identity", strategy = "increment")
    @CollectionId(columns = {@Column(name = "STOCK_ID")}, generator = "identity", type = @Type(type = "long"))
    private Collection<Stock> userStock = new ArrayList<Stock>();

    User deserializeUser() {
        File jsonFile = new File("user.json");
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(jsonFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return new Gson().fromJson(Objects.requireNonNull(fileReader), User.class);
    }

    void serializeUser(User user) {
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

//    void userUpdate() {
//        User user = new User();
//        User finalUser = user.deserializeUser();
//        Collection<Stock> userStock = finalUser.getUserStock();
//
//        LocalDateTime localDate = LocalDateTime.now();
//        String lastUpdateString = finalUser.getUserUpdate();
//
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss");
//        String dateString = localDate.format(formatter);
//        LocalDate lastUpdate = LocalDate.parse(lastUpdateString, formatter);
//
//        userStock
//                .forEach(o -> finalUser.setStockValue(o.getQuantity() * o.getPrice()));
//
//        finalUser.setWalletValue(finalUser.getStockValue() + finalUser.getBalanceAvailable());
//
//        if (localDate.getDayOfMonth() > lastUpdate.getDayOfMonth()) {
//            finalUser.setPrevWalletValue(finalUser.getWalletValue());
//        }
//
//        finalUser.setUserUpdate(dateString);
//
//        StockWIG20 stock = new StockWIG20();
//
//        userStock.stream()
//                .filter(o -> o.getTicker().equals(stock.getByTicker(o.getTicker()).getTicker()))
//                .forEach(o -> o.setPrice(stock.getByTicker(o.getTicker()).getPrice()));
//
//        user.serializeUser(finalUser);
//    }
}