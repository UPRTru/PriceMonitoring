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
public class ScheduledPriceUpdater {

    private static final Logger log = LoggerFactory.getLogger(ScheduledPriceUpdater.class);

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

    //    @Scheduled(fixedRate = 300_000) // 5 минут
    @Scheduled(fixedRate = 900_000) // 15 минут
    public void updatePrices() {
        try {
            int randomNumber = random.nextInt(120001);
            Thread.sleep(randomNumber);
            priceService.updatePrices(TypePrice.METAL, metalAgent);
            randomNumber = random.nextInt(140001) + 80000;
            Thread.sleep(randomNumber);
            priceService.updatePrices(TypePrice.CURRENCY, currencyAgent);
            log.info("Prices updated successfully.");
        } catch (Exception e) {
            log.error("Error during price update", e);
        }
    }
}