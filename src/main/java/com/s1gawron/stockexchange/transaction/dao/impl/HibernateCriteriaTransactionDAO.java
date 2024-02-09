package com.s1gawron.stockexchange.transaction.dao.impl;

import com.s1gawron.stockexchange.transaction.dao.TransactionDAO;
import com.s1gawron.stockexchange.transaction.model.Transaction;
import com.s1gawron.stockexchange.transaction.model.TransactionStatus;
import com.s1gawron.stockexchange.transaction.model.Transaction_;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class HibernateCriteriaTransactionDAO implements TransactionDAO {

    private final EntityManager entityManager;

    public HibernateCriteriaTransactionDAO(final EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override public void saveTransaction(final Transaction transaction) {
        getSession().persist(transaction);
    }

    @Override public List<Long> getNewTransactionIds() {
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<Long> query = cb.createQuery(Long.class);
        final Root<Transaction> root = query.from(Transaction.class);

        query.select(root.get(Transaction_.transactionId))
            .where(cb.equal(root.get(Transaction_.transactionStatus), TransactionStatus.NEW));

        return getSession().createQuery(query).list();
    }

    @Override public void changeTransactionsStatus(final List<Long> transactionIds, final TransactionStatus newTransactionStatus) {
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaUpdate<Transaction> criteriaUpdate = cb.createCriteriaUpdate(Transaction.class);
        final Root<Transaction> root = criteriaUpdate.from(Transaction.class);

        criteriaUpdate.set(Transaction_.transactionStatus, newTransactionStatus)
            .where(root.get(Transaction_.transactionId).in(transactionIds));

        getSession().createMutationQuery(criteriaUpdate).executeUpdate();
    }

    private Session getSession() {
        return entityManager.unwrap(Session.class);
    }
}
