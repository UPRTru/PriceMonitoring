package general.service.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Service
public final class EmailService {

    private static final String DEFAULT_FROM = "api_email@example.com";
    private static final String DEFAULT_SUBJECT = "Обновление цены";

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = Objects.requireNonNull(mailSender, "JavaMailSender cannot be null");
    }

    public void sendEmail(String to, String subject, Message body) throws MessagingException {
        Objects.requireNonNull(to, "Recipient email cannot be null");
        Objects.requireNonNull(subject, "Subject cannot be null");
        Objects.requireNonNull(body, "Message body cannot be null");

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(
                message,
                MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                StandardCharsets.UTF_8.name()
        );

        helper.setTo(to);
        helper.setFrom(DEFAULT_FROM);
        helper.setSubject(subject);
        helper.setText(buildBodyMessage(body), false);

        mailSender.send(message);
    }

    private String buildBodyMessage(Message body) {
        return "Name: " + body.name() + "\n" +
                "Buy price: " + body.buyPrice() + "\n" +
                "Sell price: " + body.sellPrice() + "\n";
    }

    public void sendEmail(String to, Message body) throws MessagingException {
        sendEmail(to, DEFAULT_SUBJECT, body);
    }

    public record Message(String name, BigDecimal buyPrice, BigDecimal sellPrice) {
        public Message {
            Objects.requireNonNull(name, "Name cannot be null");
            Objects.requireNonNull(buyPrice, "Buy price cannot be null");
            Objects.requireNonNull(sellPrice, "Sell price cannot be null");

            if (name.isBlank()) {
                throw new IllegalArgumentException("Name cannot be empty");
            }
            if (buyPrice.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Buy price must be non-negative");
            }
            if (sellPrice.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Sell price must be non-negative");
            }
        }

        public static Message of(String name, BigDecimal buyPrice, BigDecimal sellPrice) {
            return new Message(name, buyPrice, sellPrice);
        }
    }
}