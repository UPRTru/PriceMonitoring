package com.precious.shared.enums;

import java.util.Arrays;
import java.util.Optional;

public enum CurrentPrice {
    BUY("buy"),
    SELL("sell");

    private final String value;

    CurrentPrice(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Optional<CurrentPrice> fromValue(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        return Arrays.stream(CurrentPrice.values())
                .filter(cp -> cp.getValue().equalsIgnoreCase(value))
                .findFirst();
    }
}
