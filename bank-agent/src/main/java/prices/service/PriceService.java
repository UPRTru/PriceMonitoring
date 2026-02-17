package prices.service;

import com.precious.shared.dto.Price;
import com.precious.shared.enums.Banks;
import com.precious.shared.enums.TypePrice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import prices.agent.Agent;
import prices.agent.AgentConfig;
import prices.model.CurrencyPrice;
import prices.model.MetalPrice;
import prices.repository.CurrencyPriceRepository;
import prices.repository.MetalPriceRepository;

import java.util.Map;

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
        Map<String, Price> current = agent.getPrices();

        for (Map.Entry<String, Price> entry : current.entrySet()) {
            String name = entry.getKey();
            Price price = entry.getValue();

            switch (typePrice) {
                case METAL -> {
                    var latest = metalPriceRepository.findLatestByName(name);
                    MetalPrice newPrice = new MetalPrice(name, price.buyPrice(), price.sellPrice(), Banks.SBER.name());
                    if (latest.isEmpty() || !latest.get().equals(newPrice)) {
                        metalPriceRepository.save(newPrice);
                    }
                }
                case CURRENCY -> {
                    var latest = currencyPriceRepository.findLatestByName(name);
                    CurrencyPrice newPrice = new CurrencyPrice(name, price.buyPrice(), price.sellPrice(), Banks.SBER.name());
                    if (latest.isEmpty() || !latest.get().equals(newPrice)) {
                        currencyPriceRepository.save(newPrice);
                    }
                }
            }
        }
    }
}