package general.client;

import shared.dto.Price;
import shared.enums.Banks;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

@Component
public final class BankAgentClient {

    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    private static final String METAL_PATH = "/metal";
    private static final String CURRENCY_PATH = "/currency";
    private static final String ALL_PATH = "/all";
    private static final String LAST_PRICE_PATH = "/lastprice";
    private static final String HISTORY_PATH = "/history";

    private final WebClient webClient;
    private final String bankAgentUrl;

    public BankAgentClient(@Value("${bank.agent.url}") String bankAgentUrl,
                           WebClient.Builder webClientBuilder) {
        this.bankAgentUrl = validateUrl(bankAgentUrl);
        this.webClient = createWebClient(this.bankAgentUrl, webClientBuilder);
    }

    private String validateUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("bank.agent.url cannot be empty");
        }
        return url.trim();
    }

    private WebClient createWebClient(String url, WebClient.Builder builder) {
        return builder
                .baseUrl(url)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                .build();
    }

    public Mono<String> getStatus() {
        return webClient.get()
                .uri("/health")
                .retrieve()
                .bodyToMono(String.class)
                .timeout(TIMEOUT)
                .onErrorResume(e -> Mono.just("UNAVAILABLE"));
    }

    public Mono<List<Price>> getAllMetal(Banks bank) {
        Objects.requireNonNull(bank, "Bank cannot be null");
        return webClient.get()
                .uri("/{bank}{path}{all}", bank.name().toLowerCase(), METAL_PATH, ALL_PATH)
                .retrieve()
                .bodyToMono(new org.springframework.core.ParameterizedTypeReference<List<Price>>() {
                })
                .timeout(TIMEOUT)
                .onErrorResume(WebClientResponseException.class, e -> Mono.just(List.of()));
    }

    public Mono<List<Price>> getAllCurrency(Banks bank) {
        Objects.requireNonNull(bank, "Bank cannot be null");
        return webClient.get()
                .uri("/{bank}{path}{all}", bank.name().toLowerCase(), CURRENCY_PATH, ALL_PATH)
                .retrieve()
                .bodyToMono(new org.springframework.core.ParameterizedTypeReference<List<Price>>() {
                })
                .timeout(TIMEOUT)
                .onErrorResume(WebClientResponseException.class, e -> Mono.just(List.of()));
    }

    public Mono<Price> getLatestMetal(Banks bank, String metalName) {
        Objects.requireNonNull(bank, "Bank cannot be null");
        Objects.requireNonNull(metalName, "MetalName cannot be null");
        return webClient.get()
                .uri("/{bank}{path}/{name}{last}",
                        bank.name().toLowerCase(), METAL_PATH, metalName, LAST_PRICE_PATH)
                .retrieve()
                .bodyToMono(Price.class)
                .timeout(TIMEOUT);
    }

    public Mono<Price> getLatestCurrency(Banks bank, String currencyName) {
        Objects.requireNonNull(bank, "Bank cannot be null");
        Objects.requireNonNull(currencyName, "CurrencyName cannot be null");
        return webClient.get()
                .uri("/{bank}{path}/{name}{last}",
                        bank.name().toLowerCase(), CURRENCY_PATH, currencyName, LAST_PRICE_PATH)
                .retrieve()
                .bodyToMono(Price.class)
                .timeout(TIMEOUT);
    }

    public Mono<List<Price>> getHistoryMetal(Banks bank, String metalName, long from, long to) {
        Objects.requireNonNull(bank, "Bank cannot be null");
        Objects.requireNonNull(metalName, "MetalName cannot be null");
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/{bank}{path}/{name}{history}")
                        .queryParam("from", from)
                        .queryParam("to", to)
                        .build(bank.name().toLowerCase(), METAL_PATH, metalName, HISTORY_PATH))
                .retrieve()
                .bodyToMono(new org.springframework.core.ParameterizedTypeReference<List<Price>>() {
                })
                .timeout(TIMEOUT)
                .onErrorResume(WebClientResponseException.class, e -> Mono.just(List.of()));
    }

    public Mono<List<Price>> getHistoryCurrency(Banks bank, String currencyName, long from, long to) {
        Objects.requireNonNull(bank, "Bank cannot be null");
        Objects.requireNonNull(currencyName, "CurrencyName cannot be null");
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/{bank}{path}/{name}{history}")
                        .queryParam("from", from)
                        .queryParam("to", to)
                        .build(bank.name().toLowerCase(), CURRENCY_PATH, currencyName, HISTORY_PATH))
                .retrieve()
                .bodyToMono(new org.springframework.core.ParameterizedTypeReference<List<Price>>() {
                })
                .timeout(TIMEOUT)
                .onErrorResume(WebClientResponseException.class, e -> Mono.just(List.of()));
    }

    public Mono<Boolean> isAvailable() {
        return getStatus()
                .map(status -> "OK".equals(status) || "UNAVAILABLE".equals(status))
                .onErrorReturn(false);
    }
}