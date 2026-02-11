package com.precious.shared.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum Metal {
    GOLD("Золото"),
    SILVER("Серебро"),
    PLATINUM("Платина"),
    PALLADIUM("Палладий");

    private final String displayName;

    Metal(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    private static final Map<String, Metal> BY_NAME = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(Metal::getDisplayName, m -> m));

    public static java.util.Optional<Metal> fromDisplayName(String name) {
        return java.util.Optional.ofNullable(BY_NAME.get(name));
    }
}