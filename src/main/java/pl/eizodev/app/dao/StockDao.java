package pl.eizodev.app.dao;

import org.hibernate.Session;
import org.hibernate.Transaction;
import pl.eizodev.app.HibernateConfig;
import pl.eizodev.app.StockWIG20;
import pl.eizodev.app.entity.Stock;
import pl.eizodev.app.entity.User;

import java.util.List;

public class StockDao {
    public void addStock(Stock stock) {
        Session session = HibernateConfig.INSTANCE.getSessionFactory().openSession();
        Transaction transaction = session.getTransaction();
        try {
            transaction.begin();
            session.save(stock);
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

    public void updateStock(Long id) {
        Session session = HibernateConfig.INSTANCE.getSessionFactory().openSession();
        Transaction transaction = session.getTransaction();
        try {
            transaction.begin();
            User user = session.find(User.class, id);
            List<Stock> userStocks = user.getUserStock();
            StockWIG20 stock = new StockWIG20();

            userStocks
                    .forEach(o -> o.setPrice(stock.getByTicker(o.getTicker()).getPrice()));

            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

    public void getStockByTicker(String ticker) {

    }

    //TO RETHINK
    public void deleteStock(Long id) {
        Session session = HibernateConfig.INSTANCE.getSessionFactory().openSession();
        Transaction transaction = session.getTransaction();
        try {
            transaction.begin();
            Stock stock = session.find(Stock.class, id);
            session.delete(stock);
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
    }
}
