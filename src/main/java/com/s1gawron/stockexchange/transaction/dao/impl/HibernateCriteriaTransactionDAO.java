package com.s1gawron.stockexchange.transaction.dao.impl;

import com.s1gawron.stockexchange.transaction.dao.TransactionDAO;
import com.s1gawron.stockexchange.transaction.model.Transaction;
import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;

@Repository
public class HibernateCriteriaTransactionDAO implements TransactionDAO {

    private final EntityManager entityManager;

    public HibernateCriteriaTransactionDAO(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override public void saveTransaction(final Transaction transaction) {
        getSession().persist(transaction);
    }

    private Session getSession() {
        return entityManager.unwrap(Session.class);
    }
}
