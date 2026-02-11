package com.precious.user.client;

import com.precious.shared.dto.CheckPrice;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class GeneralServiceClient {

    @Value("${general.service.url}")
    private String generalServiceUrl;

    private final WebClient webClient;

    public GeneralServiceClient() {
        this.webClient = WebClient.create(generalServiceUrl);
    }

    public void checkScheduledPrice(String email, CheckPrice checkPrice) {
        webClient.post()
                .uri("/check/?email=" + email)
                .bodyValue(checkPrice)
                .retrieve();
    }

    public Long getLustUpdatePrices() {
        return webClient.get()
                .uri("/lust_update")
                .retrieve()
                .bodyToMono(Long.class)
                .block();
    }
}