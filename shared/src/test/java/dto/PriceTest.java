package dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import shared.dto.Price;
import shared.enums.Banks;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Price тесты")
class PriceTest {

    @Test
    @DisplayName("Создание Price с текущим временем")
    void shouldCreatePriceWithCurrentTime() {
        long before = System.currentTimeMillis();

        Price price = Price.createWithCurrentTime(
                Banks.SBER,
                "USD",
                new BigDecimal("90.50"),
                new BigDecimal("92.50")
        );

        assertThat(price.bank()).isEqualTo(Banks.SBER);
        assertThat(price.name()).isEqualTo("USD");
        assertThat(price.buyPrice()).isEqualByComparingTo(new BigDecimal("90.50"));
        assertThat(price.sellPrice()).isEqualByComparingTo(new BigDecimal("92.50"));
        assertThat(price.timestamp()).isGreaterThanOrEqualTo(before);
    }

    @Test
    @DisplayName("Создание Price с явным timestamp")
    void shouldCreatePriceWithExplicitTimestamp() {
        long timestamp = 1234567890L;

        Price price = Price.createWithTimestamp(
                Banks.SBER,
                "EUR",
                new BigDecimal("95.00"),
                new BigDecimal("97.00"),
                timestamp
        );

        assertThat(price.timestamp()).isEqualTo(timestamp);
    }

    @Test
    @DisplayName("Конвертация timestamp в Instant")
    void shouldConvertTimestampToInstant() {
        long timestamp = 1234567890L;
        Price price = Price.createWithTimestamp(
                Banks.SBER,
                "USD",
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                timestamp
        );

        assertThat(price.instant().toEpochMilli()).isEqualTo(timestamp);
    }

    @Test
    @DisplayName("Выброс исключения для отрицательной цены")
    void shouldThrowExceptionForNegativePrice() {
        assertThatThrownBy(() -> Price.createWithCurrentTime(
                Banks.SBER,
                "USD",
                new BigDecimal("-10.00"),
                new BigDecimal("92.50")
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Builder создает валидный Price")
    void shouldCreateValidPriceWithBuilder() {
        Price price = Price.builder()
                .bank(Banks.SBER)
                .name("GBP")
                .buyPrice(new BigDecimal("110.00"))
                .sellPrice(new BigDecimal("112.00"))
                .build();

        assertThat(price.bank()).isEqualTo(Banks.SBER);
        assertThat(price.name()).isEqualTo("GBP");
    }
}