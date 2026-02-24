package service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import prices.agent.Agent;
import prices.model.CurrencyPrice;
import prices.model.MetalPrice;
import prices.repository.CurrencyPriceRepository;
import prices.repository.MetalPriceRepository;
import prices.service.PriceService;
import shared.dto.Price;
import shared.enums.Banks;
import shared.enums.TypePrice;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PriceService тесты")
class PriceServiceTest {

    @Mock
    private MetalPriceRepository metalRepository;

    @Mock
    private CurrencyPriceRepository currencyRepository;

    @Mock
    private Agent agent;

    private PriceService priceService;

    @BeforeEach
    void setUp() {
        priceService = new PriceService(metalRepository, currencyRepository);
    }

    @Test
    @DisplayName("Обновление цен металлов - новая цена")
    void shouldUpdateMetalPricesWhenNewPrice() {
        Price newPrice = Price.createWithCurrentTime(
                Banks.SBER,
                "GOLD",
                new BigDecimal("5000.00"),
                new BigDecimal("5100.00")
        );
        when(agent.getPrices()).thenReturn(Map.of("GOLD", newPrice));
        when(metalRepository.findLatestByName("GOLD")).thenReturn(Optional.empty());

        priceService.updatePrices(TypePrice.METAL, agent);

        verify(metalRepository).save(any(MetalPrice.class));
    }

    @Test
    @DisplayName("Обновление цен металлов - цена не изменилась")
    void shouldNotUpdateMetalPricesWhenSamePrice() {
        Price samePrice = Price.createWithCurrentTime(
                Banks.SBER,
                "GOLD",
                new BigDecimal("5000.00"),
                new BigDecimal("5100.00")
        );

        when(agent.getPrices()).thenReturn(Map.of("GOLD", samePrice));
        when(metalRepository.findLatestByName("GOLD")).thenReturn(Optional.of(
                new MetalPrice("GOLD", new BigDecimal("5000.00"), new BigDecimal("5100.00"), Banks.SBER.name())
        ));

        priceService.updatePrices(TypePrice.METAL, agent);

        verify(metalRepository, never()).save(any());
    }

    @Test
    @DisplayName("Обновление цен валют")
    void shouldUpdateCurrencyPrices() {
        Price newPrice = Price.createWithCurrentTime(
                Banks.SBER,
                "USD",
                new BigDecimal("90.00"),
                new BigDecimal("92.00")
        );
        when(agent.getPrices()).thenReturn(Map.of("USD", newPrice));
        when(currencyRepository.findLatestByName("USD")).thenReturn(Optional.empty());

        priceService.updatePrices(TypePrice.CURRENCY, agent);

        verify(currencyRepository).save(any(CurrencyPrice.class));
    }

    @Test
    @DisplayName("Выброс исключения для null Agent")
    void shouldThrowExceptionForNullAgent() {
        assertThatThrownBy(() -> priceService.updatePrices(TypePrice.METAL, null))
                .isInstanceOf(NullPointerException.class);
    }
}