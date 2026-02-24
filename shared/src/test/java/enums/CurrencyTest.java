package enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import shared.enums.Banks;
import shared.enums.Currency;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Currency enum тесты")
class CurrencyTest {

    @Test
    @DisplayName("Поиск валюты по коду")
    void shouldFindCurrencyByCode() {
        Optional<Currency> result = Currency.fromCode("USD");

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(Currency.USD);
    }

    @Test
    @DisplayName("Поиск валюты по displayName")
    void shouldFindCurrencyByDisplayName() {
        Optional<Currency> result = Currency.fromDisplayName("Доллар США");

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(Currency.USD);
    }

    @Test
    @DisplayName("Возврат пустого Optional для несуществующей валюты")
    void shouldReturnEmptyForNonExistentCurrency() {
        Optional<Currency> result = Currency.fromCode("INVALID");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Получение валют по банку")
    void shouldGetCurrenciesByBank() {
        List<Currency> currencies = Currency.getCurrencyByBanks(Banks.SBER);

        assertThat(currencies).isNotEmpty();
        assertThat(currencies).contains(Currency.USD, Currency.EUR);
    }

    @Test
    @DisplayName("Проверка поддержки валюты банком")
    void shouldCheckCurrencySupport() {
        assertThat(Currency.USD.isSupportedBy(Banks.SBER)).isTrue();
        assertThat(Currency.TRY.isSupportedBy(Banks.SBER)).isFalse();
    }

    @Test
    @DisplayName("Список банков неизменяемый")
    void shouldReturnUnmodifiableBanksList() {
        List<Banks> banks = Currency.USD.getBanks();

        assertThatThrownBy(() -> banks.add(Banks.SBER))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}