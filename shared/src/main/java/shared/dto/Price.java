package shared.dto;

import shared.enums.Banks;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public record Price(Banks bank, String name, BigDecimal buyPrice, BigDecimal sellPrice, long timestamp) {

    public Price {
        Objects.requireNonNull(bank, "Bank cannot be null");
        Objects.requireNonNull(name, "Name cannot be null");
        Objects.requireNonNull(buyPrice, "Buy price cannot be null");
        Objects.requireNonNull(sellPrice, "Sell price cannot be null");

        String trimmedName = name.trim();
        if (trimmedName.isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        if (buyPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Buy price must be non-negative");
        }
        if (sellPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Sell price must be non-negative");
        }
        if (timestamp <= 0) {
            throw new IllegalArgumentException("Timestamp must be positive");
        }
    }

    public static Price createWithCurrentTime(Banks bank, String name,
                                              BigDecimal buyPrice, BigDecimal sellPrice) {
        return new Price(bank, name, buyPrice, sellPrice, System.currentTimeMillis());
    }

    public static Price createWithTimestamp(Banks bank, String name,
                                            BigDecimal buyPrice, BigDecimal sellPrice, long timestamp) {
        return new Price(bank, name, buyPrice, sellPrice, timestamp);
    }

    public static class Builder {
        private Banks bank;
        private String name;
        private BigDecimal buyPrice;
        private BigDecimal sellPrice;
        private long timestamp = System.currentTimeMillis();

        public Builder bank(Banks bank) {
            this.bank = bank;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder buyPrice(BigDecimal buyPrice) {
            this.buyPrice = buyPrice;
            return this;
        }

        public Builder sellPrice(BigDecimal sellPrice) {
            this.sellPrice = sellPrice;
            return this;
        }

        public Builder timestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder timestamp(Instant instant) {
            this.timestamp = instant.toEpochMilli();
            return this;
        }

        public Price build() {
            return new Price(bank, name, buyPrice, sellPrice, timestamp);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public Instant instant() {
        return Instant.ofEpochMilli(timestamp);
    }
}