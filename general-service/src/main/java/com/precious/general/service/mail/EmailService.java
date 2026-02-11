package com.precious.general.service.mail;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public record Message(String name, BigDecimal buyPrice, BigDecimal sellPrice) {}

    public void sendEmail(String to, String subject, Message body) throws Exception {
        String messageText = buildBodyMessage(body);
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message,
                MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                StandardCharsets.UTF_8.name());
        helper.setTo(to); // кому
        helper.setFrom("api_email"); // от кого
        helper.setSubject(subject); // тема
        helper.setText(messageText, false); // сообщение (true указывает, что тело содержит HTML)
        mailSender.send(message);
    }


    private String buildBodyMessage(Message body) {
        return "Name: " + body.name() + "\n" +
                "Buy price: " + body.buyPrice() + "\n" +
                "Sell price: " + body.sellPrice() + "\n";
    }
}
