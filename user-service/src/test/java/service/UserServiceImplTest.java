package service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import shared.dto.AuthDto;
import shared.exception.NotFoundException;
import user.model.User;
import user.repository.ScheduledPriceRepository;
import user.repository.UserRepository;
import user.service.UserServiceImpl;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl тесты")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ScheduledPriceRepository scheduledPriceRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository, scheduledPriceRepository, passwordEncoder);
    }

    @Test
    @DisplayName("Регистрация нового пользователя")
    void shouldRegisterNewUser() {
        AuthDto authDto = AuthDto.of("test@example.com", "password123");
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        User user = userService.register(authDto, "UTC");

        assertThat(user.getEmail()).isEqualTo("test@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Выброс исключения при регистрации существующего пользователя")
    void shouldThrowExceptionForExistingUser() {
        AuthDto authDto = AuthDto.of("existing@example.com", "password123");
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(authDto, "UTC"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("Аутентификация успешна")
    void shouldAuthenticateSuccessfully() {
        User user = new User("test@example.com", "hashedPassword", "UTC");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(true);

        boolean result = userService.authenticate("test@example.com", "password123");

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Аутентификация неудачна")
    void shouldAuthenticateFailed() {
        User user = new User("test@example.com", "hashedPassword", "UTC");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "hashedPassword")).thenReturn(false);

        boolean result = userService.authenticate("test@example.com", "wrongPassword");

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Выброс исключения для несуществующего пользователя")
    void shouldThrowExceptionForNonExistentUser() {
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.authenticate("notfound@example.com", "password"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("Валидация невалидного email через AuthDto")
    void shouldThrowExceptionForInvalidEmail() {
        // AuthDto валидирует email и выбрасывает IllegalArgumentException
        assertThatThrownBy(() -> AuthDto.of("invalid-email", "password123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid email format");
    }

    @Test
    @DisplayName("Валидация null email в AuthDto")
    void shouldThrowExceptionForNullEmail() {
        assertThatThrownBy(() -> AuthDto.of(null, "password123"))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Валидация короткого пароля в AuthDto")
    void shouldThrowExceptionForShortPassword() {
        assertThatThrownBy(() -> AuthDto.of("test@example.com", "short"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least 8 characters");
    }

    @Test
    @DisplayName("Получение пользователя по email")
    void shouldFindByEmail() {
        User user = new User("test@example.com", "hashedPassword", "UTC");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        Optional<User> result = userService.findByEmail("test@example.com");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Получение пустого Optional для несуществующего пользователя")
    void shouldReturnEmptyForNonExistentUser() {
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        Optional<User> result = userService.findByEmail("notfound@example.com");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Получение запланированных цен пользователя")
    void shouldGetScheduledPrices() {
        when(scheduledPriceRepository.findByUserEmail("test@example.com")).thenReturn(java.util.List.of());

        var result = userService.getScheduledPrice("test@example.com");

        assertThat(result).isEmpty();
        verify(scheduledPriceRepository).findByUserEmail("test@example.com");
    }

    @Test
    @DisplayName("Выброс исключения для null email в getScheduledPrice")
    void shouldThrowExceptionForNullEmailInGetScheduledPrice() {
        assertThatThrownBy(() -> userService.getScheduledPrice(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Удаление запланированной цены")
    void shouldDeleteScheduledPrice() {
        User user = new User("test@example.com", "hashedPassword", "UTC");
        user.getClass(); // Для инициализации

        var scheduledPrice = new user.model.ScheduledPrice(
                user,
                "SBER",
                "CURRENCY",
                "BUY",
                "USD",
                java.math.BigDecimal.valueOf(90.00)
        );

        when(scheduledPriceRepository.findById(1L)).thenReturn(Optional.of(scheduledPrice));

        userService.deleteScheduledPrice(1L, "test@example.com");

        verify(scheduledPriceRepository).delete(scheduledPrice);
    }

    @Test
    @DisplayName("Выброс исключения при удалении чужой цены")
    void shouldThrowExceptionWhenDeletingOthersPrice() {
        User user = new User("other@example.com", "hashedPassword", "UTC");
        var scheduledPrice = new user.model.ScheduledPrice(
                user,
                "SBER",
                "CURRENCY",
                "BUY",
                "USD",
                java.math.BigDecimal.valueOf(90.00)
        );

        when(scheduledPriceRepository.findById(1L)).thenReturn(Optional.of(scheduledPrice));

        assertThatThrownBy(() -> userService.deleteScheduledPrice(1L, "test@example.com"))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Unauthorized");
    }

    @Test
    @DisplayName("Удаление пользователя")
    void shouldDeleteUser() {
        User user = new User("test@example.com", "hashedPassword", "UTC");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        userService.deleteUser("test@example.com");

        verify(scheduledPriceRepository).deleteByUser(user);
        verify(userRepository).delete(user);
    }

    @Test
    @DisplayName("Выброс исключения при удалении несуществующего пользователя")
    void shouldThrowExceptionWhenDeletingNonExistentUser() {
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUser("notfound@example.com"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("Получение часового пояса пользователя")
    void shouldGetUserTimezone() {
        User user = new User("test@example.com", "hashedPassword", "UTC");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        String timezone = userService.getUserZoneDateTime("test@example.com");

        assertThat(timezone).isEqualTo("UTC");
    }

    @Test
    @DisplayName("Выброс исключения для null email в register")
    void shouldThrowExceptionForNullEmailInRegister() {
        assertThatThrownBy(() -> userService.register(null, "UTC"))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Выброс исключения для null timezone в register")
    void shouldThrowExceptionForNullTimezoneInRegister() {
        AuthDto authDto = AuthDto.of("test@example.com", "password123");

        assertThatThrownBy(() -> userService.register(authDto, null))
                .isInstanceOf(NullPointerException.class);
    }
}