package com.s1gawron.stockexchange.user.dao.impl;

import com.s1gawron.stockexchange.user.model.User;
import com.s1gawron.stockexchange.user.model.User_;
import com.s1gawron.stockexchange.user.dao.UserDAO;
import com.s1gawron.stockexchange.user.dao.filter.UserFilterParam;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class HibernateCriteriaUserDAO implements UserDAO {

    private final EntityManager entityManager;

    public HibernateCriteriaUserDAO(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override public List<Long> getAllUserIds() {
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<Long> query = cb.createQuery(Long.class);
        final Root<User> root = query.from(User.class);

        query.select(root.get(User_.id));

        return getSession().createQuery(query).list();
    }

    @Override public Optional<User> findByFilter(final UserFilterParam filterParam) {
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<User> query = cb.createQuery(User.class);
        final Root<User> root = query.from(User.class);

        query.select(root);
        filterParam.getUsername().ifPresent(param -> query.where(cb.equal(root.get(User_.username), param)));
        filterParam.getEmail().ifPresent(param -> query.where(cb.equal(root.get(User_.email), param)));

        return getSession().createQuery(query).uniqueResultOptional();
    }

    @Override public void saveUser(final User user) {
        getSession().persist(user);
    }

    private Session getSession() {
        return entityManager.unwrap(Session.class);
    }
}
