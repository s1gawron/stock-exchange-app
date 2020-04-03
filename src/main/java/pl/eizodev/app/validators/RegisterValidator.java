package pl.eizodev.app.validators;

import org.hibernate.Session;
import org.hibernate.Transaction;
import pl.eizodev.app.HibernateConfig;

import javax.persistence.Query;
import java.math.BigInteger;
import java.util.List;

public class RegisterValidator {
    public boolean userEmailExists(String email) {
        Session session = HibernateConfig.INSTANCE.getSessionFactory().openSession();
        Transaction transaction = session.getTransaction();
        List<BigInteger> resultList = null;

        try {
            transaction.begin();
            String sql = "SELECT id FROM user WHERE email = ?";
            Query query = session.createNativeQuery(sql);
            query.setParameter(1, email);
            resultList = query.getResultList();
            System.out.println(resultList);
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }

        if (resultList != null) {
            return true;
        } else {
            return false;
        }
    }

    public boolean userNameExists(String name) {
        Session session = HibernateConfig.INSTANCE.getSessionFactory().openSession();
        Transaction transaction = session.getTransaction();
        List<BigInteger> resultList = null;

        try {
            transaction.begin();
            String sql = "SELECT id FROM user WHERE name = ?";
            Query query = session.createNativeQuery(sql);
            query.setParameter(1, name);
            resultList = query.getResultList();
            System.out.println(resultList);
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }

        if (resultList != null) {
            return true;
        } else {
            return false;
        }
    }
}
