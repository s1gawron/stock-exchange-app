import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.*;

import java.io.*;
import java.util.List;
import java.util.Objects;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
class User {
    private String name;
    private double stockValue;
    private double balanceAvailable;
    private double walletValue;
    private double prevWalletValue;
    private List<StockWIG20> userStock;

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
}
