package pl.eizodev.app.dao;

import org.hibernate.Session;
import org.hibernate.Transaction;
import pl.eizodev.app.HibernateConfig;
import pl.eizodev.app.entity.Stock;
import pl.eizodev.app.entity.User;

import java.time.LocalDate;
import java.util.List;

public class UserDao {
    public User getUser(Long id) {
        Session session = HibernateConfig.INSTANCE.getSessionFactory().openSession();
        User user = session.get(User.class, id);
        session.close();
        return user;
    }

    public void addUser(User user) {
        Session session = HibernateConfig.INSTANCE.getSessionFactory().openSession();
        Transaction transaction = session.getTransaction();
        try {
            transaction.begin();
            session.save(user);
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

    public void updateUser(Long id, User newUser) {
        Session session = HibernateConfig.INSTANCE.getSessionFactory().openSession();
        Transaction transaction = session.getTransaction();
        try {
            transaction.begin();
            User user = session.find(User.class, id);
            List<Stock> userStocks = user.getUserStock();

            userStocks
                    .forEach(o -> user.setStockValue(o.getQuantity() * o.getPrice()));

            user.setWalletValue(user.getStockValue() + user.getBalanceAvailable());

            if (LocalDate.now().isAfter(user.getUserUpdate())) {
                user.setPrevWalletValue(user.getWalletValue());
            }

            user.setUserUpdate(LocalDate.now());
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
    }
}