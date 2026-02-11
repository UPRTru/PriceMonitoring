package com.precious.shared.enums;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public enum Currency {
    USD("Доллар США", List.of(Banks.SBER)),
    EUR("Евро", List.of(Banks.SBER)),
    JPY("Японская иена", List.of(Banks.SBER)),
    CNY("Китайский юань", List.of(Banks.SBER)),
    AED("Дирхам ОАЭ", List.of(Banks.SBER)),
    BYN("Белорусский рубль", List.of(Banks.SBER)),
    KZT("Казахстанский тенге", List.of(Banks.SBER)),
    GBP("Фунт стерлингов Соединенного Королевства", List.of(Banks.SBER)),
    SGD("Сингапурский доллар", List.of(Banks.SBER)),
    CHF("Швейцарский франк", List.of(Banks.SBER)),
    HKD("Гонконгский доллар", List.of(Banks.SBER)),
    CZK("Чешская крона", List.of(Banks.SBER)),
    AUD("Австралийский доллар", List.of(Banks.SBER)),
    KRW("Вона Республики Корея", List.of(Banks.SBER)),

    TRY("Турецкая лира", List.of()),
    PLN("Польский злотый", List.of()),
    THB("Таиландский бат", List.of()),
    SEK("Шведская крона", List.of()),
    NOK("Норвежская крона", List.of()),
    CAD("Канадский доллар", List.of()),
    DKK("Датская крона", List.of());

    private final String displayName;
    private final List<Banks> banks;

    Currency(String displayName, List<Banks> banks) {
        this.displayName = displayName;
        this.banks = banks;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<Banks> getBanks() {
        return banks;
    }

    private static final Map<String, Currency> BY_NAME = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(Currency::getDisplayName, m -> m));

    public static java.util.Optional<Currency> fromDisplayName(String name) {
        return java.util.Optional.ofNullable(BY_NAME.get(name));
    }

    public static List<Currency> getCurrencyByBanks(Banks bank) {
        List<Currency> result = new ArrayList<>();
        for (Currency c : values()) {
            if (c.getBanks().contains(bank)) {
                result.add(c);
            }
        }
        return result;
    }

}