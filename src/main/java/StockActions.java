import java.util.Random;

public class StockActions {

    void stockPurchase(double stockPrice, double quantity) throws InterruptedException {
        if (User.getUserBalance() >= quantity*stockPrice){
            Random random = new Random();
            int time = random.nextInt(301);
            Thread.sleep(time*1000);
            //User.getUserBalance() = User.getUserBalance() - (quantity*stockPrice);
        }
    }
}
