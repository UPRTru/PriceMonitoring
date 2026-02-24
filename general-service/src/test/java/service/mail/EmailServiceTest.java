package service.mail;

import general.service.mail.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DisplayName("EmailService Message тесты")
class EmailServiceTest {

    @Test
    @DisplayName("Создание валидного Message")
    void shouldCreateValidMessage() {
        EmailService.Message message = EmailService.Message.of(
                "USD",
                new BigDecimal("90.00"),
                new BigDecimal("92.00")
        );

        assertThat(message.name()).isEqualTo("USD");
        assertThat(message.buyPrice()).isEqualByComparingTo(new BigDecimal("90.00"));
    }

    @Test
    @DisplayName("Выброс исключения для null имени")
    void shouldThrowExceptionForNullName() {
        assertThatThrownBy(() -> EmailService.Message.of(null, BigDecimal.TEN, BigDecimal.TEN))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Выброс исключения для пустого имени")
    void shouldThrowExceptionForEmptyName() {
        assertThatThrownBy(() -> EmailService.Message.of(" ", BigDecimal.TEN, BigDecimal.TEN))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Выброс исключения для отрицательной цены")
    void shouldThrowExceptionForNegativePrice() {
        assertThatThrownBy(() -> EmailService.Message.of("USD", new BigDecimal("-10"), BigDecimal.TEN))
                .isInstanceOf(IllegalArgumentException.class);
    }
}