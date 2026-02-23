package com.precious.user.client;

import com.precious.shared.dto.CheckPrice;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Objects;

@Component
public final class GeneralServiceClient {

    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    private static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    private final WebClient webClient;

    public GeneralServiceClient(@Value("${general.service.url}") String generalServiceUrl,
                                WebClient.Builder webClientBuilder) {
        this.webClient = createWebClient(generalServiceUrl, webClientBuilder);
    }

    private WebClient createWebClient(String url, WebClient.Builder builder) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("general.service.url cannot be empty");
        }
        return builder
                .baseUrl(url.trim())
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                .build();
    }

    public void checkScheduledPrice(String email, CheckPrice checkPrice) {
        validateEmail(email);
        Objects.requireNonNull(checkPrice, "CheckPrice cannot be null");
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
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        if (!email.matches(EMAIL_REGEX)) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }
}