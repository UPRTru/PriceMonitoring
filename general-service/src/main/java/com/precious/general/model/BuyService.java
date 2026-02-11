package com.precious.general.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.time.ZoneId;

@Entity
@Table(name = "buy_service")
public class BuyService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private final String nameService;

    @Column(nullable = false)
    private final String email;

    @Column(nullable = false)
    private final Long endTime;

    public BuyService(String nameService, String email, Long buyDey) {
        this.nameService = nameService;
        this.email = email;
        //todo добавление дней покупки сервисов в секундах
        this.endTime = Instant.now().atZone(ZoneId.of("UTC")).toInstant().plusSeconds(buyDey).toEpochMilli();
    }
}
