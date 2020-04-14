//package pl.eizodev.app.dao;
//
//import org.hibernate.Hibernate;
//import org.hibernate.Session;
//import org.hibernate.Transaction;
//import pl.eizodev.app.HibernateConfig;
//import pl.eizodev.app.entity.Stock;
//import pl.eizodev.app.entity.User;
//
//import java.time.LocalDate;
//import java.util.List;
//
//public class UserDao {
//    public void updateUser(Long id) {
//        Session session = HibernateConfig.INSTANCE.getSessionFactory().openSession();
//        Transaction transaction = session.getTransaction();
//
//        try {
//            transaction.begin();
//            User user = session.find(User.class, id);
//            List<Stock> userStocks = user.getUserStock();
//            float stockValue = 0;
//
//            for (Stock stock : userStocks) {
//                stockValue += (stock.getQuantity() * stock.getPrice());
//            }
//
//            user.setStockValue(stockValue);
//
//            if (LocalDate.now().isAfter(user.getUserUpdate())) {
//                user.setPrevWalletValue(user.getWalletValue());
//            }
//
//            user.setUserUpdate(LocalDate.now());
//            user.setWalletValue(user.getStockValue() + user.getBalanceAvailable());
//            user.setWalletPercChange((user.getWalletValue() - user.getPrevWalletValue()) / user.getPrevWalletValue());
//            transaction.commit();
//        } catch (Exception e) {
//            transaction.rollback();
//            e.printStackTrace();
//        } finally {
//            session.close();
//        }
//    }
//}