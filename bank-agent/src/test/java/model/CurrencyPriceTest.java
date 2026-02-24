package model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import prices.model.CurrencyPrice;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DisplayName("CurrencyPrice Entity тесты")
class CurrencyPriceTest {

    @Test
    @DisplayName("Создание валидной CurrencyPrice")
    void shouldCreateValidCurrencyPrice() {
        CurrencyPrice price = new CurrencyPrice(
                "USD",
                new BigDecimal("90.00"),
                new BigDecimal("92.00"),
                "SBER"
        );

        assertThat(price.getName()).isEqualTo("USD");
        assertThat(price.getBuyPrice()).isEqualByComparingTo(new BigDecimal("90.00"));
        assertThat(price.getBank().name()).isEqualTo("SBER");
    }

    @Test
    @DisplayName("Выброс исключения для null имени")
    void shouldThrowExceptionForNullName() {
        assertThatThrownBy(() -> new CurrencyPrice(null, BigDecimal.TEN, BigDecimal.TEN, "SBER"))
                .isInstanceOf(NullPointerException.class);
    }
}