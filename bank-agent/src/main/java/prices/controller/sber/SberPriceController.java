package prices.controller.sber;

import com.precious.shared.dto.Price;
import org.springframework.web.bind.annotation.*;
import prices.builder.PriceBuilder;
import prices.model.CurrencyPrice;
import prices.model.MetalPrice;
import prices.repository.CurrencyPriceRepository;
import prices.repository.MetalPriceRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/sber")
public final class SberPriceController {

    //todo исключения

    private final MetalPriceRepository metalRepository;
    private final CurrencyPriceRepository currencyRepository;

    public SberPriceController(MetalPriceRepository metalRepository,
                               CurrencyPriceRepository currencyRepository) {
        this.metalRepository = metalRepository;
        this.currencyRepository = currencyRepository;
    }

    @GetMapping("/metal/lastprice/{metalName}")
    public Price getSberLatestMetal(@PathVariable String metalName) {
        Optional<MetalPrice> metalPrice = metalRepository.findLatestByName(metalName);
        if (metalPrice.isEmpty()) {
            throw new RuntimeException();
        }
        return PriceBuilder.buildPrice(metalPrice.get());
    }

    @GetMapping("/metal/all")
    public List<Price> getSberAllMetal() {
        return metalRepository.findLatestUniqueByName()
                .stream().map(PriceBuilder::buildPrice).collect(Collectors.toList());
    }

    @GetMapping("/metal/history/{metalName}")
    public List<Price> getHistoryMetal(
            @PathVariable String metalName,
            @RequestParam Long from,
            @RequestParam Long to) {
        return metalRepository.findByNameAndTimestampBetweenOrderByTimestampAsc(metalName, from, to)
                .stream().map(PriceBuilder::buildPrice).collect(Collectors.toList());
    }

    @GetMapping("/currency/lastprice/{currencyName}")
    public Price getSberLatestCurrency(@PathVariable String currencyName) {
        Optional<CurrencyPrice> currencyPrice = currencyRepository.findLatestByName(currencyName);
        if (currencyPrice.isEmpty()) {
            throw new RuntimeException();
        }
        return PriceBuilder.buildPrice(currencyPrice.get());
    }

    @GetMapping("/currency/all")
    public List<Price> getSberAllCurrency() {
        return currencyRepository.findLatestUniqueByName()
                .stream().map(PriceBuilder::buildPrice).collect(Collectors.toList());
    }

    @GetMapping("/currency/history/{currencyName}")
    public List<Price> getHistoryCurrency(
            @PathVariable String currencyName,
            @RequestParam Long from,
            @RequestParam Long to) {
        return currencyRepository.findByNameAndTimestampBetweenOrderByTimestampAsc(currencyName, from, to)
                .stream().map(PriceBuilder::buildPrice).collect(Collectors.toList());
    }
}