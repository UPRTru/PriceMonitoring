package general.service;

import general.client.BankAgentClient;
import general.service.mail.EmailService;
import general.service.mail.EmailService.Message;
import shared.dto.CheckPrice;
import shared.dto.Price;
import shared.enums.Banks;
import shared.enums.CurrentPrice;
import shared.enums.TypePrice;
import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Objects;

@Service
public final class ServiceCheckPrice {

    private static final Logger log = LoggerFactory.getLogger(ServiceCheckPrice.class);
    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    private static final String EMAIL_SUBJECT = "Обновление цены";

    private final BankAgentClient bankAgentClient;
    private final EmailService emailService;

    public ServiceCheckPrice(BankAgentClient bankAgentClient, EmailService emailService) {
        this.bankAgentClient = Objects.requireNonNull(bankAgentClient, "BankAgentClient cannot be null");
        this.emailService = Objects.requireNonNull(emailService, "EmailService cannot be null");
    }

    public void checkPrice(String email, CheckPrice checkPrice) throws MessagingException {
        Objects.requireNonNull(email, "Email cannot be null");
        Objects.requireNonNull(checkPrice, "CheckPrice cannot be null");
        validateEmail(email);

        Banks bank = checkPrice.bank();
        TypePrice typePrice = checkPrice.typePrice();
        CurrentPrice currentPrice = checkPrice.currentPrice();
        String name = checkPrice.name();
        BigDecimal targetPrice = checkPrice.price();

        Price newPrice = getNewPrice(typePrice, bank, name);

        if (newPrice == null) {
            log.warn("Could not fetch price for {} {}", typePrice, name);
            return;
        }

        if (priceComparison(currentPrice, targetPrice, newPrice.buyPrice(), newPrice.sellPrice())) {
            sendEmail(email, new Message(name, newPrice.buyPrice(), newPrice.sellPrice()));
            log.info("Price alert sent to {} for {} {}", email, typePrice, name);
        }
    }

    private void validateEmail(String email) {
        if (email == null || email.isBlank() || !email.contains("@")) {
            throw new IllegalArgumentException("Invalid email address");
        }
    }

    private Price getNewPrice(TypePrice typePrice, Banks bank, String name) {
        try {
            return switch (typePrice) {
                case METAL -> bankAgentClient.getLatestMetal(bank, name)
                        .block(TIMEOUT);
                case CURRENCY -> bankAgentClient.getLatestCurrency(bank, name)
                        .block(TIMEOUT);
            };
        } catch (Exception e) {
            log.error("Failed to fetch price for {} {} from {}", typePrice, name, bank, e);
            return null;
        }
    }

    private boolean priceComparison(CurrentPrice currentPrice, BigDecimal targetPrice,
                                    BigDecimal buyPrice, BigDecimal sellPrice) {
        Objects.requireNonNull(currentPrice, "CurrentPrice cannot be null");
        Objects.requireNonNull(targetPrice, "Target price cannot be null");
        Objects.requireNonNull(buyPrice, "Buy price cannot be null");
        Objects.requireNonNull(sellPrice, "Sell price cannot be null");

        return switch (currentPrice) {
            case BUY -> buyPrice.compareTo(targetPrice) <= 0;
            case SELL -> sellPrice.compareTo(targetPrice) >= 0;
        };
    }

    private void sendEmail(String email, Message message) throws MessagingException {
        emailService.sendEmail(email, EMAIL_SUBJECT, message);
    }
}