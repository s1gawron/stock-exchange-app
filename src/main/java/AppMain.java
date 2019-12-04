import jdk.dynalink.Operation;

public class AppMain {

    public static void main(String[] args) throws NumberFormatException {

//        User.serializeUser(new User("Seba", 0, 10000, 10000, 10000, null));
//        System.out.println(User.deserializeUser());

        System.out.println(StockWIG20.getMap());
        System.out.println(StockWIG20.getMap().get("ALR"));
    }
}
