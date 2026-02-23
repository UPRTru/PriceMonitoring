package prices.model;

import shared.enums.Banks;

import java.math.BigDecimal;

public interface Priced {

    Banks getBank();

    String getName();

    BigDecimal getBuyPrice();

    BigDecimal getSellPrice();

    Long getTimestamp();
}