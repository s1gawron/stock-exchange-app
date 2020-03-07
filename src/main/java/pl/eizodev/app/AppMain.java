package pl.eizodev.app;

import org.hibernate.Session;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import pl.eizodev.app.entity.Stock;
import pl.eizodev.app.entity.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class AppMain extends SpringBootServletInitializer {
    public static void main(String[] args) {
//        MainMenu mainMenu = new MainMenu();
//        mainMenu.start();
//        SpringApplication.run(AppMain.class, args);
        Session session = HibernateConfig.INSTANCE.getSessionFactory().openSession();

        StockWIG20 stockWig20 = new StockWIG20();
        Stock stock = stockWig20.getByTicker("CDR");
        stock.setQuantity(20);
        List<Stock> userStock = new ArrayList<>();

        User user = new User("testSebastian", LocalDate.now(), 0, 10000, 10000, 10000, userStock);
        user.addStockToList(stock);
        session.save(user);
        session.save(stock);
        session.close();
    }
}