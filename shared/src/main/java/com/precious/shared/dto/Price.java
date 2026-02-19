package com.precious.shared.dto;

import com.precious.shared.enums.Banks;

import java.math.BigDecimal;

public record Price(Banks bank, String name, BigDecimal buyPrice, BigDecimal sellPrice, long timestamp) {

    public Price {
        if (bank == null) {
            throw new IllegalArgumentException("Bank cannot be null");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        if (buyPrice == null) {
            throw new IllegalArgumentException("Buy price cannot be null");
        }
        if (buyPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Buy price must be non-negative");
        }
        if (sellPrice == null) {
            throw new IllegalArgumentException("Sell price cannot be null");
        }
        if (sellPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Sell price must be non-negative");
        }
        if (timestamp <= 0) {
            throw new IllegalArgumentException("Timestamp must be positive");
        }
    }

    public static Price of(Banks bank, String name, BigDecimal buyPrice, BigDecimal sellPrice) {
        return new Price(bank, name, buyPrice, sellPrice, System.currentTimeMillis());
    }

    public static Price of(Banks bank, String name, BigDecimal buyPrice, BigDecimal sellPrice, long timestamp) {
        return new Price(bank, name, buyPrice, sellPrice, timestamp);
    }
}
