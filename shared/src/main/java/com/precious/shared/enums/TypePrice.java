package com.precious.shared.enums;

import java.util.Arrays;

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

    public static TypePrice getTypePrice(String value) {
        return Arrays.stream(TypePrice.values()).filter(typePrice -> typePrice.getValue().equals(value))
                .findFirst()
                .orElse(null);
    }
}
