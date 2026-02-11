package prices.model;

import com.precious.shared.enums.Banks;

import java.math.BigDecimal;

public interface Priced {

    public Banks getBank();

    public String getName();

    public BigDecimal getBuyPrice();

    public BigDecimal getSellPrice();

    public Long getTimestamp();
}
