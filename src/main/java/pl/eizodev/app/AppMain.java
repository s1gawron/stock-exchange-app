package pl.eizodev.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class AppMain extends SpringBootServletInitializer {
    public static void main(String[] args) {
//        MainMenu mainMenu = new MainMenu();
//        mainMenu.start();
//        SpringApplication.run(AppMain.class, args);
        UserDao userDao = new UserDao();
        User user = new User();
        user = user.deserializeUser();
        userDao.addUser(user);
    }
}