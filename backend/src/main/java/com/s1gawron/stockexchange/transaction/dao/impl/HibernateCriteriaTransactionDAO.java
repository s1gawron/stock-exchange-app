package com.s1gawron.stockexchange.transaction.dao.impl;

import com.s1gawron.stockexchange.transaction.dao.TransactionDAO;
import com.s1gawron.stockexchange.transaction.model.Transaction;
import com.s1gawron.stockexchange.transaction.model.TransactionExecution;
import com.s1gawron.stockexchange.transaction.model.TransactionExecution_;
import com.s1gawron.stockexchange.transaction.model.TransactionStatus;
import com.s1gawron.stockexchange.transaction.model.TransactionStockBlock;
import com.s1gawron.stockexchange.transaction.model.TransactionStockBlock_;
import com.s1gawron.stockexchange.transaction.model.Transaction_;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public class HibernateCriteriaTransactionDAO implements TransactionDAO {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void saveTransaction(final Transaction transaction) {
        entityManager.persist(transaction);
    }

    @Override
    public Optional<Transaction> getTransactionById(final long transactionId) {
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<Transaction> query = cb.createQuery(Transaction.class);
        final Root<Transaction> root = query.from(Transaction.class);

        query.select(root)
            .where(cb.equal(root.get(Transaction_.id), transactionId));

        return entityManager.createQuery(query).getResultList().stream().findFirst();
    }

    @Override
    public void updateTransaction(final Transaction transaction) {
        entityManager.merge(transaction);
    }

    @Override
    public List<Transaction> getTransactionsByWalletId(final long walletId) {
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<Transaction> query = cb.createQuery(Transaction.class);
        final Root<Transaction> root = query.from(Transaction.class);

        query.select(root)
            .where(cb.equal(root.get(Transaction_.walletId), walletId))
            .orderBy(cb.desc(root.get(Transaction_.transactionCreateDate)), cb.desc(root.get(Transaction_.id)));

        return entityManager.createQuery(query).getResultList();
    }

    @Override
    public List<Transaction> getOpenTransactions() {
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<Transaction> query = cb.createQuery(Transaction.class);
        final Root<Transaction> root = query.from(Transaction.class);

        query.select(root)
            .where(root.get(Transaction_.transactionStatus).in(TransactionStatus.NEW, TransactionStatus.PARTIALLY_FILLED))
            .orderBy(cb.asc(root.get(Transaction_.transactionCreateDate)), cb.asc(root.get(Transaction_.id)));

        return entityManager.createQuery(query).getResultList();
    }

    @Override
    public void saveTransactionExecution(final TransactionExecution transactionExecution) {
        entityManager.persist(transactionExecution);
    }

    @Override
    public List<TransactionExecution> getTransactionExecutions(final Collection<Long> transactionIds) {
        if (transactionIds.isEmpty()) {
            return List.of();
        }

        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<TransactionExecution> query = cb.createQuery(TransactionExecution.class);
        final Root<TransactionExecution> root = query.from(TransactionExecution.class);

        query.select(root)
            .where(root.get(TransactionExecution_.transactionId).in(transactionIds))
            .orderBy(cb.asc(root.get(TransactionExecution_.executionDate)), cb.asc(root.get(TransactionExecution_.id)));

        return entityManager.createQuery(query).getResultList();
    }

    @Override
    public void saveTransactionStockBlock(final TransactionStockBlock transactionStockBlock) {
        entityManager.persist(transactionStockBlock);
    }

    @Override
    public List<TransactionStockBlock> getTransactionStockBlocks(final long transactionId) {
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<TransactionStockBlock> query = cb.createQuery(TransactionStockBlock.class);
        final Root<TransactionStockBlock> root = query.from(TransactionStockBlock.class);

        query.select(root)
            .where(cb.equal(root.get(TransactionStockBlock_.transactionId), transactionId))
            .orderBy(cb.asc(root.get(TransactionStockBlock_.id)));

        return entityManager.createQuery(query).getResultList();
    }

    @Override
    public void updateTransactionStockBlock(final TransactionStockBlock transactionStockBlock) {
        entityManager.merge(transactionStockBlock);
    }

    @Override
    public void deleteTransactionStockBlock(final TransactionStockBlock transactionStockBlock) {
        entityManager.remove(entityManager.contains(transactionStockBlock) ? transactionStockBlock : entityManager.merge(transactionStockBlock));
    }
}
