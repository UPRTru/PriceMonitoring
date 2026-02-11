package com.precious.shared.dto;

import com.precious.shared.enums.Banks;

import java.math.BigDecimal;

public record Price(Banks bank, String name, BigDecimal buyPrice, BigDecimal sellPrice, long timestamp) {
}
