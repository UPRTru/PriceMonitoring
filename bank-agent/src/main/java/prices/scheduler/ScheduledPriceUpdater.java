package prices.scheduler;

import com.precious.shared.enums.TypePrice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import prices.agent.Agent;
import prices.agent.sber.currency.CurrencySberAgent;
import prices.agent.sber.metal.MetalSberAgent;
import prices.service.PriceService;

import java.util.Random;

@Component
public final class ScheduledPriceUpdater {

    private static final Logger log = LoggerFactory.getLogger(ScheduledPriceUpdater.class);
    private static final int METAL_DELAY_MAX = 120001;
    private static final int CURRENCY_DELAY_MIN = 80000;
    private static final int CURRENCY_DELAY_MAX = 220001;
    private static final long UPDATE_INTERVAL_MS = 900_000;

    private final Random random = new Random();
    private final Agent metalAgent;
    private final Agent currencyAgent;
    private final PriceService priceService;

    public ScheduledPriceUpdater(@Qualifier(MetalSberAgent.AGENT_NAME) Agent metalAgent,
                                 @Qualifier(CurrencySberAgent.AGENT_NAME) Agent currencyAgent,
                                 PriceService priceService) {
        this.metalAgent = metalAgent;
        this.currencyAgent = currencyAgent;
        this.priceService = priceService;
    }

    @Scheduled(fixedRate = UPDATE_INTERVAL_MS)
    public void updatePrices() {
        try {
            Thread.sleep(random.nextInt(METAL_DELAY_MAX));
            priceService.updatePrices(TypePrice.METAL, metalAgent);

            Thread.sleep(CURRENCY_DELAY_MIN + random.nextInt(CURRENCY_DELAY_MAX - CURRENCY_DELAY_MIN));
            priceService.updatePrices(TypePrice.CURRENCY, currencyAgent);

            log.info("Prices updated successfully.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Price update interrupted", e);
        } catch (Exception e) {
            log.error("Error during price update", e);
        }
    }
}