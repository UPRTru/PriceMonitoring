package com.precious.user.model;

import com.precious.shared.dto.CheckPrice;
import com.precious.shared.enums.Banks;
import com.precious.shared.enums.CurrentPrice;
import com.precious.shared.enums.TypePrice;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "scheduler_price", indexes = {
        @Index(name = "idx_scheduled_price_user", columnList = "user_id"),
        @Index(name = "idx_scheduled_price_created", columnList = "createdAt")
})
public class ScheduledPrice {

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
    private LocalDateTime createdAt;

    public ScheduledPrice() {
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public ScheduledPrice(User user, String bank, String typePrice, String currentPrice,
                          String name, BigDecimal price) {
        this.user = user;
        this.bank = bank;
        this.typePrice = typePrice;
        this.currentPrice = currentPrice;
        this.name = name;
        this.price = price;
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
        return Objects.equals(id, that.id) &&
                Objects.equals(user, that.user) &&
                Objects.equals(name, that.name) &&
                Objects.equals(price, that.price);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, user, name, price);
    }

    @Override
    public String toString() {
        return "ScheduledPrice{" +
                ", bank='" + bank + '\'' +
                ", typePrice='" + typePrice + '\'' +
                ", currentPrice='" + currentPrice + '\'' +
                ", name='" + name + '\'' +
                ", price=" + price +
                '}';
    }

    public CheckPrice getCheckPrice() {
        return CheckPrice.of(Banks.valueOf(bank), TypePrice.valueOf(typePrice), CurrentPrice.valueOf(currentPrice), name, price);
    }
}
