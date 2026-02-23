package prices.controller.sber;

import shared.dto.Price;
import shared.exception.NotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import prices.builder.PriceBuilder;
import prices.repository.CurrencyPriceRepository;
import prices.repository.MetalPriceRepository;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/sber")
public final class SberPriceController {

    private final MetalPriceRepository metalRepository;
    private final CurrencyPriceRepository currencyRepository;

    public SberPriceController(MetalPriceRepository metalRepository,
                               CurrencyPriceRepository currencyRepository) {
        this.metalRepository = metalRepository;
        this.currencyRepository = currencyRepository;
    }

    @GetMapping("/metal/lastprice/{metalName}")
    public ResponseEntity<Price> getSberLatestMetal(@PathVariable String metalName) {
        return metalRepository.findLatestByName(metalName)
                .map(PriceBuilder::buildPrice)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new NotFoundException("Metal not found: " + metalName));
    }

    @GetMapping("/metal/all")
    public ResponseEntity<List<Price>> getSberAllMetal() {
        List<Price> prices = metalRepository.findLatestUniqueByName()
                .stream()
                .map(PriceBuilder::buildPrice)
                .collect(Collectors.toUnmodifiableList());
        return ResponseEntity.ok(prices);
    }

    @GetMapping("/metal/history/{metalName}")
    public ResponseEntity<List<Price>> getHistoryMetal(
            @PathVariable String metalName,
            @RequestParam Long from,
            @RequestParam Long to) {
        List<Price> prices = metalRepository.findByNameAndTimestampBetweenOrderByTimestampAsc(metalName, from, to)
                .stream()
                .map(PriceBuilder::buildPrice)
                .collect(Collectors.toUnmodifiableList());
        return ResponseEntity.ok(prices);
    }

    @GetMapping("/currency/lastprice/{currencyName}")
    public ResponseEntity<Price> getSberLatestCurrency(@PathVariable String currencyName) {
        return currencyRepository.findLatestByName(currencyName)
                .map(PriceBuilder::buildPrice)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new NotFoundException("Currency not found: " + currencyName));
    }

    @GetMapping("/currency/all")
    public ResponseEntity<List<Price>> getSberAllCurrency() {
        List<Price> prices = currencyRepository.findLatestUniqueByName()
                .stream()
                .map(PriceBuilder::buildPrice)
                .collect(Collectors.toUnmodifiableList());
        return ResponseEntity.ok(prices);
    }

    @GetMapping("/currency/history/{currencyName}")
    public ResponseEntity<List<Price>> getHistoryCurrency(
            @PathVariable String currencyName,
            @RequestParam Long from,
            @RequestParam Long to) {
        List<Price> prices = currencyRepository.findByNameAndTimestampBetweenOrderByTimestampAsc(currencyName, from, to)
                .stream()
                .map(PriceBuilder::buildPrice)
                .collect(Collectors.toUnmodifiableList());
        return ResponseEntity.ok(prices);
    }
}