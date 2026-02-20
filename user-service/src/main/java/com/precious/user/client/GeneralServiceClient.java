package com.precious.user.client;

import com.precious.shared.dto.CheckPrice;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Component
public final class GeneralServiceClient {

    private final WebClient webClient;
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    public GeneralServiceClient(@Value("${general.service.url}") String generalServiceUrl,
                                WebClient.Builder webClientBuilder) {
        if (generalServiceUrl == null || generalServiceUrl.isBlank()) {
            throw new IllegalArgumentException("general.service.url cannot be empty");
        }
        this.webClient = webClientBuilder
                .baseUrl(generalServiceUrl)
                .build();
    }

    public void checkScheduledPrice(String email, CheckPrice checkPrice) {
        validateEmail(email);
        webClient.post()
                .uri("/check/{email}", email)
                .bodyValue(checkPrice)
                .retrieve()
                .toBodilessEntity()
                .timeout(TIMEOUT)
                .block();
    }

    public Long getLastUpdatePrices() {
        return webClient.get()
                .uri("/last_update")
                .retrieve()
                .bodyToMono(Long.class)
                .timeout(TIMEOUT)
                .block();
    }

    private void validateEmail(String email) {
        if (email == null || email.isBlank() || !email.contains("@")) {
            throw new IllegalArgumentException("Invalid email address");
        }
    }
}