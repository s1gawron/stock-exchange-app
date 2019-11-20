import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Objects;

public class User {
    private String name;
    private double stockValue;
    private double balanceAvailable;
    private double walletValue;
    private double prevWalletValue;
    private JsonElement stock;

    public User(String name, double stockValue, double balanceAvailable, double walletValue, double prevWalletValue, JsonElement stock) {
        this.name = name;
        this.stockValue = stockValue;
        this.balanceAvailable = balanceAvailable;
        this.walletValue = walletValue;
        this.prevWalletValue = prevWalletValue;
        this.stock = stock;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", stockValue=" + stockValue +
                ", balanceAvailable=" + balanceAvailable +
                ", walletValue=" + walletValue +
                ", prevWalletValue=" + prevWalletValue +
                ", stock=" + stock +
                '}';
    }

    static{
        try {
            File jsonFile = new File(Objects.requireNonNull(User.class.getClassLoader().getResource("user.json")).getFile());
            FileReader fileReader = new FileReader(jsonFile);
            JsonObject jsonObject = new Gson().fromJson(fileReader, JsonObject.class);

            String jsonUserName = String.valueOf(jsonObject.get("name"));
            double jsonStockValue = Double.parseDouble(String.valueOf(jsonObject.get("stockValue")));
            double jsonBalanceAvailable = Double.parseDouble(String.valueOf(jsonObject.get("balanceAvailable")));
            double  jsonWalletValue = Double.parseDouble(String.valueOf(jsonObject.get("walletValue")));
            double jsonPrevStockValue = Double.parseDouble(String.valueOf(jsonObject.get("prevWalletValue")));
            JsonElement jsonStock = jsonObject.get("stock");

            User test = new User(jsonUserName, jsonStockValue, jsonBalanceAvailable, jsonWalletValue, jsonPrevStockValue, jsonStock);

            System.out.println(test);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    double getUserBalance (){
        return User.this.balanceAvailable;
    }
}
