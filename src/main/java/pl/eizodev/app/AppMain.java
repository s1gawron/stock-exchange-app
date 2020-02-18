package pl.eizodev.app;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import pl.eizodev.app.dao.StockDao;
import pl.eizodev.app.dao.UserDao;
import pl.eizodev.app.entity.Stock;
import pl.eizodev.app.entity.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@SpringBootApplication
public class AppMain extends SpringBootServletInitializer {
    public static void main(String[] args) {
//        MainMenu mainMenu = new MainMenu();
//        mainMenu.start();
//        SpringApplication.run(AppMain.class, args);
        UserDao userDao = new UserDao();
        StockDao stockDao = new StockDao();
        StockWIG20 stockWig20 = new StockWIG20();
        Stock stock = stockWig20.getByTicker("CDR");
        stock.setQuantity(10);
        User user = new User("testSebastian", LocalDate.now(), 0, 10000, 10000, 10000, null);
        List<Stock> userStock = new ArrayList<>();
        userStock.add(stock);
        user.setUserStock(userStock);

        userDao.addUser(user);
        stockDao.addStock(stock);
        System.out.println(userDao.getUser(1L));
    }
}