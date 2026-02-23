package shared.enums;

import java.util.*;
import java.util.function.Function;
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
        this.banks = Collections.unmodifiableList(banks);
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<Banks> getBanks() {
        return banks;
    }

    private static final Map<String, Currency> BY_DISPLAY_NAME = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(
                    Currency::getDisplayName,
                    Function.identity()
            ));

    private static final Map<String, Currency> BY_CODE = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(
                    Currency::name,
                    Function.identity(),
                    (existing, replacement) -> existing
            ));

    public static Optional<Currency> fromDisplayName(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(BY_DISPLAY_NAME.get(name));
    }

    public static Optional<Currency> fromCode(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(BY_CODE.get(code.toUpperCase()));
    }

    public static List<Currency> getCurrencyByBanks(Banks bank) {
        if (bank == null) {
            return List.of();
        }
        return Arrays.stream(values())
                .filter(c -> c.getBanks().contains(bank))
                .collect(Collectors.toUnmodifiableList());
    }

    public boolean isSupportedBy(Banks bank) {
        return bank != null && this.banks.contains(bank);
    }
}