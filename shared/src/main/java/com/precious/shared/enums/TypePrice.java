package com.precious.shared.enums;

import java.util.Arrays;
import java.util.Optional;

public enum TypePrice {
    CURRENCY("currency"),
    METAL("metal");

    private final String value;

    TypePrice(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Optional<TypePrice> fromValue(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        return Arrays.stream(TypePrice.values())
                .filter(tp -> tp.getValue().equalsIgnoreCase(value))
                .findFirst();
    }
}
