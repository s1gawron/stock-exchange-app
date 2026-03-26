package com.s1gawron.stockexchange.user.stock.favourite.dao.impl;

import com.s1gawron.stockexchange.user.stock.favourite.dao.UserFavouriteStockDAO;
import com.s1gawron.stockexchange.user.stock.favourite.model.UserFavouriteStock;
import com.s1gawron.stockexchange.user.stock.favourite.model.UserFavouriteStock_;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class HibernateCriteriaUserFavouriteStockDAO implements UserFavouriteStockDAO {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<UserFavouriteStock> findByUserId(final long userId) {
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<UserFavouriteStock> query = cb.createQuery(UserFavouriteStock.class);
        final Root<UserFavouriteStock> root = query.from(UserFavouriteStock.class);

        query.select(root).where(cb.equal(root.get(UserFavouriteStock_.userId), userId));

        return entityManager.createQuery(query).getResultList();
    }

    @Override
    public Optional<UserFavouriteStock> findByUserIdAndTicker(final long userId, final String ticker) {
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<UserFavouriteStock> query = cb.createQuery(UserFavouriteStock.class);
        final Root<UserFavouriteStock> root = query.from(UserFavouriteStock.class);

        query.select(root).where(
            cb.equal(root.get(UserFavouriteStock_.userId), userId),
            cb.equal(root.get(UserFavouriteStock_.ticker), ticker)
        );

        return entityManager.createQuery(query).getResultList().stream().findFirst();
    }

    @Override
    public void save(final UserFavouriteStock userFavouriteStock) {
        entityManager.persist(userFavouriteStock);
    }

    @Override
    public void delete(final UserFavouriteStock userFavouriteStock) {
        entityManager.remove(userFavouriteStock);
    }
}
