public class AppMain {

    public static void main(String[] args) throws NumberFormatException {
        StockWIG20.getWig20();
        Json.editJson("ALIOR", 19.34, 4);
        System.out.println(Json.getJson());
        //User.getUserBalance();
    }
}
