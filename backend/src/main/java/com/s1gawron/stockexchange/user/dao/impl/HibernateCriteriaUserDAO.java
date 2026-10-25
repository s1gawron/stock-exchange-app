package com.s1gawron.stockexchange.user.dao.impl;

import com.s1gawron.stockexchange.user.model.User;
import com.s1gawron.stockexchange.user.model.User_;
import com.s1gawron.stockexchange.user.dao.UserDAO;
import com.s1gawron.stockexchange.user.dao.filter.UserFilterParam;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Repository
public class HibernateCriteriaUserDAO implements UserDAO {

    @PersistenceContext
    private EntityManager entityManager;

    @Override public Optional<User> findByFilter(final UserFilterParam filterParam) {
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<User> query = cb.createQuery(User.class);
        final Root<User> root = query.from(User.class);

        final List<Predicate> predicates = new ArrayList<>();
        filterParam.getUsername().ifPresent(param -> predicates.add(cb.equal(root.get(User_.username), param)));
        filterParam.getEmail().ifPresent(param -> predicates.add(cb.equal(cb.lower(root.get(User_.email)), param.toLowerCase(Locale.ROOT))));

        if (predicates.isEmpty()) {
            return Optional.empty();
        }

        query.select(root).where(predicates.toArray(Predicate[]::new));

        return entityManager.createQuery(query).getResultList().stream().findFirst();
    }

    @Override public void saveUser(final User user) {
        entityManager.persist(user);
    }
}
