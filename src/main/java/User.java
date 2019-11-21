import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class User {
    private String name;
    private double stockValue;
    private double balanceAvailable;
    private double walletValue;
    private double prevWalletValue;
    private JsonElement stock;

    User(String name, double stockValue, double balanceAvailable, double walletValue, double prevWalletValue, JsonElement stock) {
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

//    static void getUserBalance() {
//        String jsonUserName = String.valueOf(Json.getJson().get("name"));
//        double jsonStockValue = Double.parseDouble(String.valueOf(Json.getJson().get("stockValue")));
//        double jsonBalanceAvailable = Double.parseDouble(String.valueOf(Json.getJson().get("balanceAvailable")));
//        double  jsonWalletValue = Double.parseDouble(String.valueOf(Json.getJson().get("walletValue")));
//        double jsonPrevStockValue = Double.parseDouble(String.valueOf(Json.getJson().get("prevWalletValue")));
//        JsonElement jsonStock = Json.getJson().get("stock");
//
//        User test = new User(jsonUserName, jsonStockValue, jsonBalanceAvailable, jsonWalletValue, jsonPrevStockValue, jsonStock);
//        System.out.println(test);

        //return test.balanceAvailable;
   // }
}
