package com.precious.general.scheduler;

import com.precious.general.client.BankAgentClient;
import com.precious.shared.dto.Price;
import com.precious.shared.enums.Banks;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Component
public class PriceCheckScheduler {

    public static Long lustUpdate = Instant.now().toEpochMilli();
    private final BankAgentClient bankAgentClient;

    public PriceCheckScheduler(BankAgentClient bankAgentClient) {
        this.bankAgentClient = bankAgentClient;
    }

    @Scheduled(fixedRate = 30000) // каждые 30 сек
    public void checkPrices() {
        try {
            List<Price> allMetalPriceList = bankAgentClient.getAllMetal(Banks.SBER).block(Duration.ofSeconds(10));
            List<Price> allCurrencyPriceList = bankAgentClient.getAllCurrency(Banks.SBER).block(Duration.ofSeconds(10));
            lustUpdate = Instant.now().toEpochMilli();
            //todo выводить данные на фронт
        } catch (Exception e) {
            System.err.println("Ошибка при проверке цен: " + e.getMessage());
        }
    }
}