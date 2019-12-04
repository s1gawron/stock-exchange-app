import jdk.dynalink.Operation;

public class AppMain {

    public static void main(String[] args) throws NumberFormatException {

//        User.serializeUser(new User("Seba", 0, 10000, 10000, 10000, null));
//        System.out.println(User.deserializeUser());

        User.deserializeUser().setName("test");
        User.deserializeUser().setBalanceAvailable(1111);
        User.deserializeUser().setWalletValue(1111);
        User.serializeUser(User.deserializeUser());
    }
}
