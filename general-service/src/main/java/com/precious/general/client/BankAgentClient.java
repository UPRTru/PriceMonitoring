package com.precious.general.client;

import com.precious.shared.dto.Price;
import com.precious.shared.enums.Banks;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public final class BankAgentClient {

    @Value("${bank.agent.url}")
    private String bankAgentUrl;

    private final WebClient webClient;

    public BankAgentClient(@Value("${bank.agent.url}") String bankAgentUrl,
                           WebClient.Builder webClientBuilder) {
        if (bankAgentUrl == null || bankAgentUrl.isBlank()) {
            throw new IllegalArgumentException("bank.agent.url cannot be empty");
        }
        this.webClient = webClientBuilder
                .baseUrl(bankAgentUrl)
                .build();
    }

    public String getStatus() {
        return String.valueOf(webClient.get()
                .uri("/health")
                .retrieve()
                .bodyToMono(String.class));
    }

    public Mono<List<Price>> getAllMetal(Banks bank) {
        return webClient.get()
                .uri("/" + bank.name().toLowerCase() + "/metal/all")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Price>>() {
                });
    }

    public Mono<List<Price>> getAllCurrency(Banks bank) {
        return webClient.get()
                .uri("/" + bank.name().toLowerCase() + "/currency/all")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Price>>() {
                });
    }

    public Mono<Price> getLatestMetal(Banks bank, String metalName) {
        return webClient.get()
                .uri("/" + bank.name().toLowerCase() + "/metal/lastprice/?metalName=" + metalName)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Price>() {
                });
    }

    public Mono<Price> getLatestCurrency(Banks bank, String currencyName) {
        return webClient.get()
                .uri("/" + bank.name().toLowerCase() + "/currency/lastprice/?currencyName=" + currencyName)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Price>() {
                });
    }


    public Mono<List<Price>> getHistoryMetal(Banks bank, String metalName, long from, long to) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/{bank}/metal/history/{metalName}")
                        .queryParam("from", from)
                        .queryParam("to", to)
                        .build(bank.name().toLowerCase(), metalName))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Price>>() {
                });
    }

    public Mono<List<Price>> getHistoryCurrency(Banks bank, String currencyName, long from, long to) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/{bank}/currency/history/{currencyName}")
                        .queryParam("from", from)
                        .queryParam("to", to)
                        .build(bank.name().toLowerCase(), currencyName))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Price>>() {
                });
    }
}