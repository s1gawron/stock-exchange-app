import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.Objects;

class Json {
    static Object getJsonData() {
        File jsonFile = new File((Objects.requireNonNull(User.class.getClassLoader().getResource("user.json"))).getFile());
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(jsonFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return new Gson().fromJson(Objects.requireNonNull(fileReader), User.class);
    }

//    static void editJson(String name, double price, int quantity) {
//        Json.getJson()("stock").addProperty("stockName", name);
//        Json.getJson().getAsJsonObject("stock").addProperty("stockPrice", price);
//        Json.getJson().getAsJsonObject("stock").addProperty("stockQuantity", quantity);
//    }
}
