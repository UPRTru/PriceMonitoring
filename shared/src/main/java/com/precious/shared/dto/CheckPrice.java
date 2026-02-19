package com.precious.shared.dto;

import com.precious.shared.enums.Banks;
import com.precious.shared.enums.CurrentPrice;
import com.precious.shared.enums.TypePrice;

import java.math.BigDecimal;

public record CheckPrice(Banks bank, TypePrice typePrice, CurrentPrice currentPrice, String name, BigDecimal price) {

    public CheckPrice {
        if (bank == null) {
            throw new IllegalArgumentException("Bank cannot be null");
        }
        if (typePrice == null) {
            throw new IllegalArgumentException("TypePrice cannot be null");
        }
        if (currentPrice == null) {
            throw new IllegalArgumentException("CurrentPrice cannot be null");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        if (price == null) {
            throw new IllegalArgumentException("Price cannot be null");
        }
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price must be non-negative");
        }
    }

    public static CheckPrice of(Banks bank, TypePrice typePrice, CurrentPrice currentPrice,
                                String name, BigDecimal price) {
        return new CheckPrice(bank, typePrice, currentPrice, name, price);
    }
}
