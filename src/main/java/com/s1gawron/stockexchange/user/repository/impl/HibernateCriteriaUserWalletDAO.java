package com.s1gawron.stockexchange.user.repository.impl;

import com.s1gawron.stockexchange.user.model.*;
import com.s1gawron.stockexchange.user.repository.UserWalletDAO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class HibernateCriteriaUserWalletDAO implements UserWalletDAO {

    private final EntityManager entityManager;

    public HibernateCriteriaUserWalletDAO(final EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override public Optional<UserWallet> findUserWalletByUserId(final long userId) {
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<UserWallet> query = cb.createQuery(UserWallet.class);
        final Root<UserWallet> root = query.from(UserWallet.class);

        query.select(root).where(cb.equal(root.get(UserWallet_.ownerId), userId));

        return getSession().createQuery(query).uniqueResultOptional();
    }

    @Override public List<UserStock> getUserStocks(final Long walletId) {
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<UserStock> query = cb.createQuery(UserStock.class);
        final Root<UserStock> root = query.from(UserStock.class);

        query.select(root)
            .where(
                cb.equal(root.get(UserStock_.userWalletId), walletId)
            );

        return getSession().createQuery(query).list();
    }

    @Override public void saveUserWallet(final UserWallet userWallet) {
        getSession().persist(userWallet);
    }

    @Override public void updateUserWallet(final UserWallet userWallet) {
        getSession().merge(userWallet);
    }

    private Session getSession() {
        return entityManager.unwrap(Session.class);
    }
}
