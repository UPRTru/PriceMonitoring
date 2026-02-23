package shared.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    private static final Map<String, TypePrice> BY_VALUE = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(
                    TypePrice::getValue,
                    Function.identity(),
                    (existing, replacement) -> existing
            ));

    public static Optional<TypePrice> fromValue(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(BY_VALUE.get(value.toLowerCase()));
    }

    public static boolean exists(String value) {
        return fromValue(value).isPresent();
    }
}