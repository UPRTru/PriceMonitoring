package com.precious.shared.enums;

import java.util.Arrays;

public enum Banks {
    SBER("Сбербанк");

    private final String name;

    Banks(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static Banks getBanks(String value) {
        return Arrays.stream(Banks.values()).filter(banks -> banks.getName().equals(value))
                .findFirst()
                .orElse(null);
    }
}
