import jdk.dynalink.Operation;

public class AppMain {

    public static void main(String[] args) throws NumberFormatException {

        User.serializeUser(new User("Seba", 0, 10000, 10000, 10000, null));
        User user=User.deserializeUser();
        user.setName("test");
        user.setBalanceAvailable(1111);
        user.setWalletValue(1111);
        User.serializeUser(user);
    }
}
