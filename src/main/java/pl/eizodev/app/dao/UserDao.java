package pl.eizodev.app.dao;

import org.hibernate.Session;
import org.hibernate.Transaction;
import pl.eizodev.app.HibernateConfig;
import pl.eizodev.app.entity.Stock;
import pl.eizodev.app.entity.User;

import java.time.LocalDate;
import java.util.List;

public class UserDao {
    private Session session = HibernateConfig.INSTANCE.getSessionFactory().openSession();
    private Transaction transaction = session.getTransaction();

    public User getUser(Long id) {
        User user = session.get(User.class, id);
        session.close();
        return user;
    }

    public void addUser(User user) {
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

    public void updateUser(Long id) {
        try {
            transaction.begin();
            User user = session.find(User.class, id);
            List<Stock> userStocks = user.getUserStock();
            float stockValue = 0;

            for (Stock stock: userStocks) {
                stockValue += (stock.getQuantity() * stock.getPrice());
            }

            user.setStockValue(stockValue);
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

    public void deleteUser(Long id) {
        try {
            transaction.begin();
            User user = session.find(User.class, id);
            session.delete(user);
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
    }
}