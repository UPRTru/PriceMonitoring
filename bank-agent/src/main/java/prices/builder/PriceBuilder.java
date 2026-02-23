package prices.builder;

import shared.dto.Price;
import jakarta.validation.constraints.NotNull;
import prices.model.Priced;

import java.util.Objects;
import java.util.Optional;

public final class PriceBuilder {

    private PriceBuilder() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static Price buildPrice(@NotNull Priced priced) {
        Objects.requireNonNull(priced, "Priced cannot be null");
        return Price.createWithTimestamp(
                priced.getBank(),
                priced.getName(),
                priced.getBuyPrice(),
                priced.getSellPrice(),
                priced.getTimestamp()
        );
    }

    public static Optional<Price> buildPriceSafe(Priced priced) {
        if (priced == null) {
            return Optional.empty();
        }
        return Optional.of(buildPrice(priced));
    }
}