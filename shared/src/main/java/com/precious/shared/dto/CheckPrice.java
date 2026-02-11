package com.precious.shared.dto;

import com.precious.shared.enums.Banks;
import com.precious.shared.enums.CurrentPrice;
import com.precious.shared.enums.TypePrice;

import java.math.BigDecimal;

public record CheckPrice(Banks bank, TypePrice typePrice, CurrentPrice currentPrice, String name, BigDecimal price) {
}
