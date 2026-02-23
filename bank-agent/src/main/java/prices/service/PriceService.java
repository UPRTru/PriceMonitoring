package prices.service;

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

        for (Map.Entry<String, Price> entry : currentPrices.entrySet()) {
            String name = entry.getKey();
            Price price = entry.getValue();

            Objects.requireNonNull(name, "Price name cannot be null");
            Objects.requireNonNull(price, "Price cannot be null");

            switch (typePrice) {
                case METAL -> saveMetalPrice(name, price);
                case CURRENCY -> saveCurrencyPrice(name, price);
                default -> throw new IllegalArgumentException("Unknown TypePrice: " + typePrice);
            }
        }
    }

    private void saveMetalPrice(String name, Price price) {
        metalPriceRepository.findLatestByName(name).ifPresentOrElse(
                latest -> {
                    if (!pricesEqual(latest, price)) {
                        metalPriceRepository.save(createMetalPrice(name, price));
                    }
                },
                () -> metalPriceRepository.save(createMetalPrice(name, price))
        );
    }

    private void saveCurrencyPrice(String name, Price price) {
        currencyPriceRepository.findLatestByName(name).ifPresentOrElse(
                latest -> {
                    if (!pricesEqual(latest, price)) {
                        currencyPriceRepository.save(createCurrencyPrice(name, price));
                    }
                },
                () -> currencyPriceRepository.save(createCurrencyPrice(name, price))
        );
    }

    private MetalPrice createMetalPrice(String name, Price price) {
        return new MetalPrice(name, price.buyPrice(), price.sellPrice(), Banks.SBER.name(), price.timestamp());
    }

    private CurrencyPrice createCurrencyPrice(String name, Price price) {
        return new CurrencyPrice(name, price.buyPrice(), price.sellPrice(), Banks.SBER.name(), price.timestamp());
    }

    private boolean pricesEqual(Priced existing, Price newPrice) {
        return existing.getBuyPrice().compareTo(newPrice.buyPrice()) == 0 &&
                existing.getSellPrice().compareTo(newPrice.sellPrice()) == 0;
    }
}