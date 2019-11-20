import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;

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

    static void userData() {
        String json = "/Users/eizodev/IdeaProjects/Gielda/src/main/resources/user.json";
        JsonObject jo = new Gson().fromJson(json, JsonObject.class);

        String jsonUserName = String.valueOf(jo.get("name"));
        double jsonStockValue = Double.parseDouble(String.valueOf(jo.get("stockValue")));
        double jsonBalanceAvailable = Double.parseDouble(String.valueOf(jo.get("balanceAvailable")));
        double  jsonWalletValue = Double.parseDouble(String.valueOf(jo.get("walletValue")));
        double jsonPrevStockValue = Double.parseDouble(String.valueOf(jo.get("prevWalletValue")));
        JsonElement jsonStock = jo.get("stock");

        User test = new User(jsonUserName, jsonStockValue, jsonBalanceAvailable, jsonWalletValue, jsonPrevStockValue, jsonStock);

        System.out.println(test.stock);
    }
}
