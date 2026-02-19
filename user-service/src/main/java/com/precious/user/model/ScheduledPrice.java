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

@Entity
@Table(name = "scheduler_price")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
    private java.math.BigDecimal price;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public CheckPrice getCheckPrice() {
        return CheckPrice.of(Banks.valueOf(bank), TypePrice.valueOf(typePrice), CurrentPrice.valueOf(currentPrice), name, price);
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
}
