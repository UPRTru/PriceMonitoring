package com.precious.general.service;

import com.precious.general.model.RegisteredService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ServiceRegistry {

    private final Map<String, RegisteredService> services = new ConcurrentHashMap<>();

    public void register(String name, String frontendUrl) {
        services.put(name, new RegisteredService(name, frontendUrl));
    }

    public void heartbeat(String name) {
        RegisteredService service = services.get(name);
        if (service != null) {
            service.setActive(true);
        }
    }

    public Map<String, RegisteredService> getActiveServices() {
        services.entrySet().removeIf(entry ->
                Instant.now().minusSeconds(30).isAfter(entry.getValue().getLastHeartbeat())
        );
        return services;
    }

    public boolean isUserServiceRegistered() {
        return services.containsKey("user-service") && services.get("user-service").isActive();
    }
}