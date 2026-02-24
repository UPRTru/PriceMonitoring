package service;

import general.client.BankAgentClient;
import general.service.ServiceCheckPrice;
import general.service.mail.EmailService;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import shared.dto.CheckPrice;
import shared.dto.Price;
import shared.enums.*;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ServiceCheckPrice тесты")
class ServiceCheckPriceTest {

    @Mock
    private BankAgentClient bankAgentClient;

    @Mock
    private EmailService emailService;

    private ServiceCheckPrice serviceCheckPrice;

    @BeforeEach
    void setUp() {
        serviceCheckPrice = new ServiceCheckPrice(bankAgentClient, emailService);
    }

    @Test
    @DisplayName("Отправка email при срабатывании условия BUY")
    void shouldSendEmailWhenBuyPriceConditionMet() throws MessagingException {
        CheckPrice checkPrice = CheckPrice.of(
                Banks.SBER,
                TypePrice.CURRENCY,
                CurrentPrice.BUY,
                "USD",
                new BigDecimal("95.00")
        );
        Price currentPrice = Price.createWithCurrentTime(
                Banks.SBER,
                "USD",
                new BigDecimal("90.00"),
                new BigDecimal("92.00")
        );

        when(bankAgentClient.getLatestCurrency(Banks.SBER, "USD"))
                .thenReturn(Mono.just(currentPrice));

        serviceCheckPrice.checkPrice("test@example.com", checkPrice);

        verify(emailService).sendEmail(eq("test@example.com"), anyString(), any());
    }

    @Test
    @DisplayName("Отправка email при срабатывании условия SELL")
    void shouldSendEmailWhenSellPriceConditionMet() throws MessagingException {
        CheckPrice checkPrice = CheckPrice.of(
                Banks.SBER,
                TypePrice.CURRENCY,
                CurrentPrice.SELL,
                "USD",
                new BigDecimal("85.00")
        );
        Price currentPrice = Price.createWithCurrentTime(
                Banks.SBER,
                "USD",
                new BigDecimal("90.00"),
                new BigDecimal("92.00")
        );

        when(bankAgentClient.getLatestCurrency(Banks.SBER, "USD"))
                .thenReturn(Mono.just(currentPrice));

        serviceCheckPrice.checkPrice("test@example.com", checkPrice);

        verify(emailService).sendEmail(eq("test@example.com"), anyString(), any());
    }

    @Test
    @DisplayName("Email не отправляется если условие не сработало")
    void shouldNotSendEmailWhenConditionNotMet() throws MessagingException {
        CheckPrice checkPrice = CheckPrice.of(
                Banks.SBER,
                TypePrice.CURRENCY,
                CurrentPrice.BUY,
                "USD",
                new BigDecimal("80.00")
        );
        Price currentPrice = Price.createWithCurrentTime(
                Banks.SBER,
                "USD",
                new BigDecimal("90.00"),
                new BigDecimal("92.00")
        );

        when(bankAgentClient.getLatestCurrency(Banks.SBER, "USD"))
                .thenReturn(Mono.just(currentPrice));

        serviceCheckPrice.checkPrice("test@example.com", checkPrice);

        verify(emailService, never()).sendEmail(any(), any(), any());
    }

    @Test
    @DisplayName("Выброс исключения для невалидного email")
    void shouldThrowExceptionForInvalidEmail() {
        CheckPrice checkPrice = CheckPrice.of(
                Banks.SBER,
                TypePrice.CURRENCY,
                CurrentPrice.BUY,
                "USD",
                BigDecimal.TEN
        );

        assertThatThrownBy(() -> serviceCheckPrice.checkPrice("invalid-email", checkPrice))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Выброс исключения для null CheckPrice")
    void shouldThrowExceptionForNullCheckPrice() {
        assertThatThrownBy(() -> serviceCheckPrice.checkPrice("test@example.com", null))
                .isInstanceOf(NullPointerException.class);
    }
}