package shared.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
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

    private static final Map<String, Metal> BY_DISPLAY_NAME = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(
                    Metal::getDisplayName,
                    Function.identity()
            ));

    private static final Map<String, Metal> BY_CODE = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(
                    Metal::name,
                    Function.identity(),
                    (existing, replacement) -> existing
            ));

    public static Optional<Metal> fromDisplayName(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(BY_DISPLAY_NAME.get(name));
    }

    public static Optional<Metal> fromCode(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(BY_CODE.get(code.toUpperCase()));
    }

    public static boolean existsByCode(String code) {
        return fromCode(code).isPresent();
    }
}