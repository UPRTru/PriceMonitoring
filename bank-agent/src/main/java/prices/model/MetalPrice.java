package prices.model;

import shared.enums.Banks;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Objects;

@Entity
@Table(name = "metal_prices", indexes = {
        @Index(name = "metal_prices_idx_name", columnList = "name"),
        @Index(name = "metal_prices_idx_timestamp", columnList = "timestamp")
})
public final class MetalPrice implements Priced {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal buyPrice;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal sellPrice;

    @Column(nullable = false)
    private Long timestamp = Instant.now().atZone(ZoneId.of("UTC")).toInstant().toEpochMilli();

    @Column(nullable = false)
    private String bank;

    protected MetalPrice() {
    }

    public MetalPrice(String name, BigDecimal buyPrice, BigDecimal sellPrice, String bank) {
        this.name = Objects.requireNonNull(name, "Name cannot be null");
        this.buyPrice = Objects.requireNonNull(buyPrice, "Buy price cannot be null");
        this.sellPrice = Objects.requireNonNull(sellPrice, "Sell price cannot be null");
        this.bank = Objects.requireNonNull(bank, "Bank cannot be null");
    }

    public MetalPrice(String name, BigDecimal buyPrice, BigDecimal sellPrice, String bank, Long timestamp) {
        this.name = Objects.requireNonNull(name, "Name cannot be null");
        this.buyPrice = Objects.requireNonNull(buyPrice, "Buy price cannot be null");
        this.sellPrice = Objects.requireNonNull(sellPrice, "Sell price cannot be null");
        this.bank = Objects.requireNonNull(bank, "Bank cannot be null");
        this.timestamp = Objects.requireNonNull(timestamp, "Timestamp cannot be null");
    }

    public Long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public BigDecimal getBuyPrice() {
        return buyPrice;
    }

    @Override
    public BigDecimal getSellPrice() {
        return sellPrice;
    }

    @Override
    public Long getTimestamp() {
        return timestamp;
    }

    @Override
    public Banks getBank() {
        return Banks.valueOf(bank);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MetalPrice that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "MetalPrice{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", buyPrice=" + buyPrice +
                ", sellPrice=" + sellPrice +
                ", timestamp=" + timestamp +
                ", bank='" + bank + '\'' +
                '}';
    }
}