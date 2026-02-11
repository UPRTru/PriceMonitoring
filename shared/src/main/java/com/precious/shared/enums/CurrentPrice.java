package com.precious.shared.enums;

import java.util.Arrays;

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

    public static CurrentPrice getCurrentPrice(String value) {
        return Arrays.stream(CurrentPrice.values()).filter(currentPrice -> currentPrice.getValue().equals(value))
                .findFirst()
                .orElse(null);
    }
}
