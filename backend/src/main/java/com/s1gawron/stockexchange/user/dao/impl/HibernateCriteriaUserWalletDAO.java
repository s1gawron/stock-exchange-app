package com.s1gawron.stockexchange.user.dao.impl;

import com.s1gawron.stockexchange.user.model.*;
import com.s1gawron.stockexchange.user.dao.UserWalletDAO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class HibernateCriteriaUserWalletDAO implements UserWalletDAO {

    @PersistenceContext
    private EntityManager entityManager;

    @Override public Optional<UserWallet> findById(final long walletId) {
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<UserWallet> query = cb.createQuery(UserWallet.class);
        final Root<UserWallet> root = query.from(UserWallet.class);

        query.select(root).where(cb.equal(root.get(UserWallet_.id), walletId));

        return entityManager.createQuery(query).getResultList().stream().findFirst();
    }

    @Override public Optional<UserWallet> findByIdForUpdate(final long walletId) {
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<UserWallet> query = cb.createQuery(UserWallet.class);
        final Root<UserWallet> root = query.from(UserWallet.class);

        query.select(root).where(cb.equal(root.get(UserWallet_.id), walletId));

        return entityManager.createQuery(query).setLockMode(LockModeType.PESSIMISTIC_WRITE).getResultList().stream().findFirst();
    }

    @Override public Optional<UserWallet> findUserWalletByUserId(final long userId) {
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<UserWallet> query = cb.createQuery(UserWallet.class);
        final Root<UserWallet> root = query.from(UserWallet.class);

        query.select(root).where(cb.equal(root.get(UserWallet_.userId), userId));

        return entityManager.createQuery(query).getResultList().stream().findFirst();
    }

    @Override public Optional<UserWallet> findUserWalletByUserIdForUpdate(final long userId) {
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<UserWallet> query = cb.createQuery(UserWallet.class);
        final Root<UserWallet> root = query.from(UserWallet.class);

        query.select(root).where(cb.equal(root.get(UserWallet_.userId), userId));

        return entityManager.createQuery(query).setLockMode(LockModeType.PESSIMISTIC_WRITE).getResultList().stream().findFirst();
    }

    @Override public List<UserStockPosition> getUserStockPositions(final long walletId) {
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<UserStockPosition> query = cb.createQuery(UserStockPosition.class);
        final Root<UserStockPosition> root = query.from(UserStockPosition.class);

        query.select(root)
            .where(
                cb.equal(root.get(UserStockPosition_.walletId), walletId)
            )
            .orderBy(cb.asc(root.get(UserStockPosition_.openDate)), cb.asc(root.get(UserStockPosition_.id)));

        return entityManager.createQuery(query).getResultList();
    }

    @Override public void saveUserWallet(final UserWallet userWallet) {
        entityManager.persist(userWallet);
    }

    @Override public void updateUserWallet(final UserWallet userWallet) {
        entityManager.merge(userWallet);
    }

    @Override public List<UserStockPosition> getUserStockPositions(final long walletId, final String ticker) {
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<UserStockPosition> query = cb.createQuery(UserStockPosition.class);
        final Root<UserStockPosition> root = query.from(UserStockPosition.class);

        query.select(root)
            .where(
                cb.equal(root.get(UserStockPosition_.walletId), walletId),
                cb.equal(root.get(UserStockPosition_.ticker), ticker)
            )
            .orderBy(cb.asc(root.get(UserStockPosition_.openDate)), cb.asc(root.get(UserStockPosition_.id)));

        return entityManager.createQuery(query).getResultList();
    }

    @Override public void updateUserStockPosition(final UserStockPosition userStockPosition) {
        entityManager.merge(userStockPosition);
    }

    @Override public void saveUserStockPosition(final UserStockPosition userStockPosition) {
        entityManager.persist(userStockPosition);
    }

    @Override public void deleteUserStockPosition(final UserStockPosition userStockPosition) {
        entityManager.remove(userStockPosition);
    }

    @Override public List<Long> getAllWalletIds() {
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<Long> query = cb.createQuery(Long.class);
        final Root<UserWallet> root = query.from(UserWallet.class);

        query.select(root.get(UserWallet_.id));

        return entityManager.createQuery(query).getResultList();
    }
}
