package prices.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import prices.agent.sber.currency.CurrencySberAgent;
import shared.dto.Price;
import shared.enums.Banks;
import shared.enums.TypePrice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import prices.agent.Agent;
import prices.model.CurrencyPrice;
import prices.model.MetalPrice;
import prices.model.Priced;
import prices.repository.CurrencyPriceRepository;
import prices.repository.MetalPriceRepository;

import java.util.Map;
import java.util.Objects;

@Service
public class PriceService {

    private static final Logger log = LoggerFactory.getLogger(PriceService.class);

    private final MetalPriceRepository metalPriceRepository;
    private final CurrencyPriceRepository currencyPriceRepository;

    public PriceService(MetalPriceRepository metalPriceRepository,
                        CurrencyPriceRepository currencyPriceRepository) {
        this.metalPriceRepository = metalPriceRepository;
        this.currencyPriceRepository = currencyPriceRepository;
    }

    @Transactional
    public void updatePrices(TypePrice typePrice, Agent agent) {
        Objects.requireNonNull(typePrice, "TypePrice cannot be null");
        Objects.requireNonNull(agent, "Agent cannot be null");

        Map<String, Price> currentPrices = agent.getPrices();
        Objects.requireNonNull(currentPrices, "Prices cannot be null");

        if (currentPrices.isEmpty()) {
            log.warn("No prices received from agent for {}", typePrice);
            return;
        }

        int savedCount = 0;
        int skippedCount = 0;

        for (Map.Entry<String, Price> entry : currentPrices.entrySet()) {
            String name = entry.getKey();
            Price price = entry.getValue();

            if (price == null) {
                skippedCount++;
                log.debug("Null price for {}", name);
                continue;
            }

            try {
                boolean saved = switch (typePrice) {
                    case METAL -> saveMetalPrice(name, price);
                    case CURRENCY -> saveCurrencyPrice(name, price);
                };

                if (saved) {
                    savedCount++;
                } else {
                    skippedCount++;
                }
            } catch (Exception e) {
                log.error("Failed to save price for {} {}: {}", typePrice, name, e.getMessage(), e);
                skippedCount++;
            }
        }

        log.info("Price update completed for {}: saved={}, skipped={}",
                typePrice, savedCount, skippedCount);
    }

    private boolean saveMetalPrice(String name, Price price) {
        Objects.requireNonNull(name, "Name cannot be null");
        Objects.requireNonNull(price, "Price cannot be null");

        return metalPriceRepository.findLatestByName(name)
                .map(latest -> {
                    if (!pricesEqual(latest, price)) {
                        metalPriceRepository.save(createMetalPrice(name, price));
                        log.debug("New metal price saved for {}: buy={}, sell={}",
                                name, price.buyPrice(), price.sellPrice());
                        return true;
                    }
                    log.debug("Metal price unchanged for {}", name);
                    return false;
                })
                .orElseGet(() -> {
                    metalPriceRepository.save(createMetalPrice(name, price));
                    log.debug("First metal price saved for {}", name);
                    return true;
                });
    }

    private boolean saveCurrencyPrice(String name, Price price) {
        Objects.requireNonNull(name, "Name cannot be null");
        Objects.requireNonNull(price, "Price cannot be null");

        return currencyPriceRepository.findLatestByName(name)
                .map(latest -> {
                    if (!pricesEqual(latest, price)) {
                        currencyPriceRepository.save(createCurrencyPrice(name, price));
                        log.debug("New currency price saved for {}: buy={}, sell={}",
                                name, price.buyPrice(), price.sellPrice());
                        return true;
                    }
                    log.debug("Currency price unchanged for {}", name);
                    return false;
                })
                .orElseGet(() -> {
                    currencyPriceRepository.save(createCurrencyPrice(name, price));
                    log.debug("First currency price saved for {}", name);
                    return true;
                });
    }

    private MetalPrice createMetalPrice(String name, Price price) {
        return new MetalPrice(name, price.buyPrice(), price.sellPrice(), Banks.SBER.name(), price.timestamp());
    }

    private CurrencyPrice createCurrencyPrice(String name, Price price) {
        return new CurrencyPrice(name, price.buyPrice(), price.sellPrice(), Banks.SBER.name(), price.timestamp());
    }

    private boolean pricesEqual(Priced existing, Price newPrice) {
        Objects.requireNonNull(existing, "Existing price cannot be null");
        Objects.requireNonNull(newPrice, "New price cannot be null");

        return existing.getBuyPrice().compareTo(newPrice.buyPrice()) == 0 &&
                existing.getSellPrice().compareTo(newPrice.sellPrice()) == 0;
    }
}