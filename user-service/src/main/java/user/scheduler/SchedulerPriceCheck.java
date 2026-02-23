package user.scheduler;

import user.client.GeneralServiceClient;
import user.model.ScheduledPrice;
import user.model.User;
import user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
public final class SchedulerPriceCheck {

    private static final Logger log = LoggerFactory.getLogger(SchedulerPriceCheck.class);
    private static final long CHECK_INTERVAL_MS = 300_000;
    private static final int RANDOM_DELAY_MS = 5000;

    private final GeneralServiceClient generalServiceClient;
    private final UserService userService;

    public SchedulerPriceCheck(GeneralServiceClient generalServiceClient,
                               UserService userService) {
        this.generalServiceClient = generalServiceClient;
        this.userService = userService;
    }

    @Scheduled(fixedRate = CHECK_INTERVAL_MS)
    public void checkPrices() {
        try {
            List<User> users = userService.getAllUsers();
            if (users.isEmpty()) {
                log.debug("No users to check prices for");
                return;
            }
            int successCount = 0;
            int failCount = 0;
            for (User user : users) {
                List<ScheduledPrice> scheduledPrices = user.getScheduledPrices();
                for (ScheduledPrice scheduledPrice : scheduledPrices) {
                    try {
                        Thread.sleep(ThreadLocalRandom.current().nextInt(RANDOM_DELAY_MS));
                        generalServiceClient.checkScheduledPrice(
                                user.getEmail(),
                                scheduledPrice.getCheckPrice()
                        );
                        successCount++;
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.error("Price check interrupted for user {}", user.getEmail());
                        break;
                    } catch (Exception e) {
                        failCount++;
                        log.error("Failed to check price for user {}: {}",
                                user.getEmail(), e.getMessage());
                    }
                }
            }
            log.info("Price check completed. Success: {}, Failed: {}, Total users: {}",
                    successCount, failCount, users.size());
        } catch (Exception e) {
            log.error("Error during scheduled price check", e);
        }
    }
}