package shared.dto;

import shared.enums.Banks;
import shared.enums.CurrentPrice;
import shared.enums.TypePrice;

import java.math.BigDecimal;
import java.util.Objects;

public record CheckPrice(Banks bank, TypePrice typePrice, CurrentPrice currentPrice, String name, BigDecimal price) {

    public CheckPrice {
        Objects.requireNonNull(bank, "Bank cannot be null");
        Objects.requireNonNull(typePrice, "TypePrice cannot be null");
        Objects.requireNonNull(currentPrice, "CurrentPrice cannot be null");
        Objects.requireNonNull(name, "Name cannot be null");
        Objects.requireNonNull(price, "Price cannot be null");

        String trimmedName = name.trim();
        if (trimmedName.isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price must be non-negative");
        }
    }

    public static CheckPrice of(Banks bank, TypePrice typePrice, CurrentPrice currentPrice,
                                String name, BigDecimal price) {
        return new CheckPrice(bank, typePrice, currentPrice, name, price);
    }

    public static class Builder {
        private Banks bank;
        private TypePrice typePrice;
        private CurrentPrice currentPrice;
        private String name;
        private BigDecimal price;

        public Builder bank(Banks bank) {
            this.bank = bank;
            return this;
        }

        public Builder typePrice(TypePrice typePrice) {
            this.typePrice = typePrice;
            return this;
        }

        public Builder currentPrice(CurrentPrice currentPrice) {
            this.currentPrice = currentPrice;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder price(BigDecimal price) {
            this.price = price;
            return this;
        }

        public CheckPrice build() {
            return new CheckPrice(bank, typePrice, currentPrice, name, price);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}