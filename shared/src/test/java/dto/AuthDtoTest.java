package dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import shared.dto.AuthDto;

import static org.assertj.core.api.Assertions.*;

@DisplayName("AuthDto тесты")
class AuthDtoTest {

    @Test
    @DisplayName("Создание валидного AuthDto")
    void shouldCreateValidAuthDto() {
        AuthDto authDto = AuthDto.of("test@example.com", "password123");

        assertThat(authDto.email()).isEqualTo("test@example.com");
        assertThat(authDto.password()).isEqualTo("password123");
    }

    @Test
    @DisplayName("Нормализация email")
    void shouldNormalizeEmail() {
        AuthDto authDto = AuthDto.of("  Test@Example.com  ", "password123");

        assertThat(authDto.normalizedEmail()).isEqualTo("test@example.com");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  ", "invalid", "no-at-sign"})
    @DisplayName("Выброс исключения для невалидного email")
    void shouldThrowExceptionForInvalidEmail(String email) {
        if (email == null) {
            assertThatThrownBy(() -> AuthDto.of(email, "password123"))
                    .isInstanceOf(NullPointerException.class);
        } else {
            assertThatThrownBy(() -> AuthDto.of(email, "password123"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"pass", "1234567", "short"})
    @DisplayName("Выброс исключения для короткого пароля")
    void shouldThrowExceptionForShortPassword(String password) {
        assertThatThrownBy(() -> AuthDto.of("test@example.com", password))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least 8 characters");
    }

    @Test
    @DisplayName("Выброс исключения для null пароля")
    void shouldThrowExceptionForNullPassword() {
        assertThatThrownBy(() -> AuthDto.of("test@example.com", null))
                .isInstanceOf(NullPointerException.class);
    }
}