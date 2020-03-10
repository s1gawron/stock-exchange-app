package pl.eizodev.app;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import pl.eizodev.app.dao.UserDao;
import pl.eizodev.app.entity.Stock;
import pl.eizodev.app.entity.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class AppMain extends SpringBootServletInitializer {
    public static void main(String[] args) {
//        SpringApplication.run(AppMain.class, args);
        UserDao userDao = new UserDao();
        List<Stock> userStock = new ArrayList<>();
        User user = new User("testSebastian", LocalDate.now(), 0, 10000, 10000, 10000, userStock);
//        userDao.addUser(user);
        MainMenu mainMenu = new MainMenu();
        mainMenu.menu(1L);
    }
}