package com.precious.user.scheduler;

import com.precious.user.client.GeneralServiceClient;
import com.precious.user.model.User;
import com.precious.user.service.UserServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public final class SchedulerPriceCheck {

    private static final Logger log = LoggerFactory.getLogger(SchedulerPriceCheck.class);
    private static final long CHECK_INTERVAL_MS = 300_000;

    private final GeneralServiceClient generalServiceClient;
    private final UserServiceImpl userServiceImpl;

    public SchedulerPriceCheck(GeneralServiceClient generalServiceClient,
                               UserServiceImpl userServiceImpl) {
        this.generalServiceClient = generalServiceClient;
        this.userServiceImpl = userServiceImpl;
    }

    @Scheduled(fixedRate = CHECK_INTERVAL_MS) // 5 минут
    public void checkPrices() {
        try {
            List<User> users = userServiceImpl.getAllUsers();
            for (User user : users) {
                user.getScheduledPrices().forEach(scheduledPrice -> {
                            try {
                                generalServiceClient.checkScheduledPrice(
                                        user.getEmail(),
                                        scheduledPrice.getCheckPrice()
                                );
                            } catch (Exception e) {
                                log.error("Failed to check price for user {}: {}",
                                        user.getEmail(), e.getMessage());
                            }
                        }
                );
            }
            log.info("Price check completed for {} users", users.size());
        } catch (Exception e) {
            log.error("Error during scheduled price check", e);
        }
    }
}