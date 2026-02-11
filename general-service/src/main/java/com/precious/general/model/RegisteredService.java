package com.precious.general.model;

import java.time.Instant;

public class RegisteredService {
    private final String name;
    private final String frontendUrl;
    private final Instant lastHeartbeat;
    private boolean active = true;

    public RegisteredService(String name, String frontendUrl) {
        this.name = name;
        this.frontendUrl = frontendUrl;
        this.lastHeartbeat = Instant.now();
    }

    public String getName() { return name; }

    public String getFrontendUrl() { return frontendUrl; }

    public Instant getLastHeartbeat() { return lastHeartbeat; }

    public boolean isActive() { return active; }

    public void setActive(boolean active) { this.active = active; }
}