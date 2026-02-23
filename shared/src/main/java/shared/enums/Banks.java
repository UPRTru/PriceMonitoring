package shared.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum Banks {
    SBER("Сбербанк");

    private final String displayName;

    Banks(String name) {
        this.displayName = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    private static final Map<String, Banks> BY_DISPLAY_NAME = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(
                    Banks::getDisplayName,
                    Function.identity()
            ));

    private static final Map<String, Banks> BY_CODE = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(
                    Banks::name,
                    Function.identity(),
                    (existing, replacement) -> existing
            ));

    public static Optional<Banks> findByName(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(BY_DISPLAY_NAME.get(value));
    }

    public static Optional<Banks> findByCode(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(BY_CODE.get(code.toUpperCase()));
    }

    public static boolean existsByCode(String code) {
        return findByCode(code).isPresent();
    }
}