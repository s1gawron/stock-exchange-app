package com.s1gawron.stockexchange.user.dao.impl;

import com.s1gawron.stockexchange.user.model.*;
import com.s1gawron.stockexchange.user.dao.UserWalletDAO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class HibernateCriteriaUserWalletDAO implements UserWalletDAO {

    private final EntityManager entityManager;

    public HibernateCriteriaUserWalletDAO(final EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override public Optional<UserWallet> findById(final long walletId) {
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<UserWallet> query = cb.createQuery(UserWallet.class);
        final Root<UserWallet> root = query.from(UserWallet.class);

        query.select(root).where(cb.equal(root.get(UserWallet_.id), walletId));

        return entityManager.createQuery(query).getResultList().stream().findFirst();
    }

    @Override public Optional<UserWallet> findUserWalletByUserId(final long userId) {
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<UserWallet> query = cb.createQuery(UserWallet.class);
        final Root<UserWallet> root = query.from(UserWallet.class);

        query.select(root).where(cb.equal(root.get(UserWallet_.userId), userId));

        return entityManager.createQuery(query).getResultList().stream().findFirst();
    }

    @Override public List<UserStock> getUserStocks(final long walletId) {
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<UserStock> query = cb.createQuery(UserStock.class);
        final Root<UserStock> root = query.from(UserStock.class);

        query.select(root)
            .where(
                cb.equal(root.get(UserStock_.walletId), walletId)
            );

        return entityManager.createQuery(query).getResultList();
    }

    @Override public void saveUserWallet(final UserWallet userWallet) {
        entityManager.persist(userWallet);
    }

    @Override public void updateUserWallet(final UserWallet userWallet) {
        entityManager.merge(userWallet);
    }

    @Override public Optional<UserStock> getUserStock(final long walletId, final String ticker) {
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<UserStock> query = cb.createQuery(UserStock.class);
        final Root<UserStock> root = query.from(UserStock.class);

        query.select(root)
            .where(
                cb.equal(root.get(UserStock_.walletId), walletId),
                cb.equal(root.get(UserStock_.ticker), ticker)
            );

        return entityManager.createQuery(query).getResultList().stream().findFirst();
    }

    @Override public void updateUserStock(final UserStock userStock) {
        entityManager.merge(userStock);
    }

    @Override public void saveUserStock(final UserStock userStock) {
        entityManager.persist(userStock);
    }

    @Override public void deleteUserStock(final UserStock userStock) {
        entityManager.remove(userStock);
    }

    @Override public List<Long> getAllWalletIds() {
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<Long> query = cb.createQuery(Long.class);
        final Root<UserWallet> root = query.from(UserWallet.class);

        query.select(root.get(UserWallet_.id));

        return entityManager.createQuery(query).getResultList();
    }
}
