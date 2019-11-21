import com.google.gson.JsonElement;

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

    static double getUserBalance() {
        User test = (User) Json.getJsonData();
        return test.balanceAvailable;
    }
}
