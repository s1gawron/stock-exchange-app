package com.s1gawron.stockexchange.user.stock.favourite.model;

import jakarta.persistence.*;

@Entity
@Table(name = "public__user_favourite_stock", uniqueConstraints = {
    @UniqueConstraint(columnNames = { "user_id", "ticker" })
})
public class UserFavouriteStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "ticker", nullable = false)
    private String ticker;

    protected UserFavouriteStock() {
    }

    private UserFavouriteStock(final long userId, final String ticker) {
        this.userId = userId;
        this.ticker = ticker;
    }

    public static UserFavouriteStock create(final long userId, final String ticker) {
        return new UserFavouriteStock(userId, ticker);
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getTicker() {
        return ticker;
    }
}
