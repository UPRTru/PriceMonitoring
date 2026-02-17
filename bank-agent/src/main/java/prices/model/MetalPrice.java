package prices.model;

import com.precious.shared.enums.Banks;
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
public class MetalPrice implements Priced {

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
    private final Long timestamp = Instant.now().atZone(ZoneId.of("UTC")).toInstant().toEpochMilli();

    @Column(nullable = false)
    private String bank;

    protected MetalPrice() {
    }

    public MetalPrice(String name, BigDecimal buyPrice, BigDecimal sellPrice, String bank) {
        this.name = Objects.requireNonNull(name);
        this.buyPrice = Objects.requireNonNull(buyPrice);
        this.sellPrice = Objects.requireNonNull(sellPrice);
        this.bank = bank;
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
        return Objects.equals(id, that.id) &&
                Objects.equals(name, that.name) &&
                Objects.equals(buyPrice, that.buyPrice) &&
                Objects.equals(sellPrice, that.sellPrice) &&
                Objects.equals(timestamp, that.timestamp) &&
                Objects.equals(bank, that.bank);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, buyPrice, sellPrice, timestamp, bank);
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