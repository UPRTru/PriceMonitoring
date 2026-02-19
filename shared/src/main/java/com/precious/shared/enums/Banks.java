package com.precious.shared.enums;

import java.util.Arrays;
import java.util.Optional;

public enum Banks {
    SBER("Сбербанк");

    private final String name;

    Banks(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static Optional<Banks> findByName(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        return Arrays.stream(Banks.values())
                .filter(bank -> bank.getName().equals(value))
                .findFirst();
    }

    public static Optional<Banks> findByCode(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Banks.valueOf(code.toUpperCase()));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
