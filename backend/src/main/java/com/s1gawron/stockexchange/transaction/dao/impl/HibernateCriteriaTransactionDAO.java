package com.s1gawron.stockexchange.transaction.dao.impl;

import com.s1gawron.stockexchange.transaction.dao.TransactionDAO;
import com.s1gawron.stockexchange.transaction.model.Transaction;
import com.s1gawron.stockexchange.transaction.model.Transaction_;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class HibernateCriteriaTransactionDAO implements TransactionDAO {

    private final EntityManager entityManager;

    public HibernateCriteriaTransactionDAO(final EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public void saveTransaction(final Transaction transaction) {
        getSession().persist(transaction);
    }

    @Override
    public Optional<Transaction> getTransactionById(final long transactionId) {
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<Transaction> query = cb.createQuery(Transaction.class);
        final Root<Transaction> root = query.from(Transaction.class);

        query.select(root)
            .where(cb.equal(root.get(Transaction_.transactionId), transactionId));

        return getSession().createQuery(query).uniqueResultOptional();
    }

    @Override
    public void updateTransaction(final Transaction transaction) {
        getSession().merge(transaction);
    }

    private Session getSession() {
        return entityManager.unwrap(Session.class);
    }
}
