package pl.eizodev.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class AppMain extends SpringBootServletInitializer {
    public static void main(String[] args) {
//        MainMenu mainMenu = new MainMenu();
//        mainMenu.start();
//        SpringApplication.run(AppMain.class, args);
        UserDao userDao = new UserDao();
//        List<Stock> stocks = new ArrayList<>();
//        StockWIG20 stockWig20 = new StockWIG20();
//        Stock stock = stockWig20.getByTicker("CDR");
//        stock.setQuantity(10);
//        stocks.add(stock);
//        User user = new User(1L, "testSebastian", LocalDate.now(), 0, 10000, 10000, 10000, stocks);

//        userDao.addUser(user);
        System.out.println(userDao.getUser(1L));
    }
}