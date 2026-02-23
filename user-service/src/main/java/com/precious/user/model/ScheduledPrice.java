package com.precious.user.model;

import com.precious.shared.dto.CheckPrice;
import com.precious.shared.enums.Banks;
import com.precious.shared.enums.CurrentPrice;
import com.precious.shared.enums.TypePrice;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "scheduler_price", indexes = {
        @Index(name = "idx_scheduled_price_user", columnList = "user_id"),
        @Index(name = "idx_scheduled_price_created", columnList = "createdAt")
})
public final class ScheduledPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String bank;

    @Column(nullable = false)
    private String typePrice;

    @Column(nullable = false)
    private String currentPrice;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal price;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    protected ScheduledPrice() {
    }

    public ScheduledPrice(User user, String bank, String typePrice, String currentPrice,
                          String name, BigDecimal price) {
        this.user = Objects.requireNonNull(user, "User cannot be null");
        this.bank = Objects.requireNonNull(bank, "Bank cannot be null");
        this.typePrice = Objects.requireNonNull(typePrice, "TypePrice cannot be null");
        this.currentPrice = Objects.requireNonNull(currentPrice, "CurrentPrice cannot be null");
        this.name = Objects.requireNonNull(name, "Name cannot be null");
        this.price = Objects.requireNonNull(price, "Price cannot be null");
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getBank() {
        return bank;
    }

    public String getTypePrice() {
        return typePrice;
    }

    public String getCurrentPrice() {
        return currentPrice;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ScheduledPrice that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ScheduledPrice{" +
                "id=" + id +
                ", bank='" + bank + '\'' +
                ", typePrice='" + typePrice + '\'' +
                ", currentPrice='" + currentPrice + '\'' +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", createdAt=" + createdAt +
                '}';
    }

    public CheckPrice getCheckPrice() {
        return CheckPrice.of(
                Banks.valueOf(bank),
                TypePrice.valueOf(typePrice),
                CurrentPrice.valueOf(currentPrice),
                name,
                price
        );
    }
}