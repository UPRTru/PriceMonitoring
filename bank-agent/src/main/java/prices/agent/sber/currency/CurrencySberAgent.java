package prices.agent.sber.currency;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import prices.agent.Agent;
import prices.agent.AgentConfig;
import prices.agent.EnumAgentsConfig;
import prices.agent.WebDriverSupport;
import shared.dto.Price;
import shared.enums.Banks;
import shared.enums.Currency;
import shared.enums.CurrentPrice;

import java.math.BigDecimal;
import java.util.*;

@Component(CurrencySberAgent.AGENT_NAME)
public final class CurrencySberAgent implements Agent {

    private static final Logger log = LoggerFactory.getLogger(CurrencySberAgent.class);

    public static final String AGENT_NAME = "sber agent current";
    private static final int MAX_COUNT_CURRENCY = 5;

    private final AgentConfig agentConfig;
    private final WebDriverSupport webDriverSupport;

    public CurrencySberAgent(@Qualifier("webDriverSupport") WebDriverSupport webDriverSupport) {
        this.agentConfig = EnumAgentsConfig.SBER_CURRENT.getAgentConfig();
        this.webDriverSupport = webDriverSupport;
    }

    @Override
    public Map<String, Price> getPrices() {
        webDriverSupport.createDriver();
        try {
            List<Currency> currenciesForBank = Currency.getCurrencyByBanks(Banks.SBER);
            if (currenciesForBank.isEmpty()) {
                log.warn("No currencies available for bank {}", Banks.SBER);
                return Map.of();
            }
            int iterations = calculateIterations(currenciesForBank.size());
            int index = 0;
            Map<String, Price> result = new HashMap<>(currenciesForBank.size());
            while (iterations > 0) {
                List<Currency> batch = getNextBatch(currenciesForBank, index);
                String url = buildUrl(batch);
                log.debug("Fetching currencies batch: {}",
                        batch.stream().map(Currency::name).toList());
                webDriverSupport.goToPage(url);
                for (Currency currency : batch) {
                    try {
                        Price price = fetchCurrencyPrice(currency);
                        if (price != null) {
                            result.put(currency.name(), price);
                        }
                    } catch (Exception e) {
                        log.error("Failed to fetch price for currency {}: {}",
                                currency.name(), e.getMessage());
                    }
                }
                index += MAX_COUNT_CURRENCY;
                iterations--;
            }
            log.info("Successfully fetched prices for {} currencies", result.size());
            return result;
        } finally {
            webDriverSupport.close();
        }
    }

    @Override
    public String getName() {
        return AGENT_NAME;
    }

    private int calculateIterations(int totalCurrencies) {
        if (totalCurrencies <= 0) {
            return 0;
        }
        return (int) Math.ceil((double) totalCurrencies / MAX_COUNT_CURRENCY);
    }

    private List<Currency> getNextBatch(List<Currency> allCurrencies, int startIndex) {
        List<Currency> batch = new ArrayList<>(MAX_COUNT_CURRENCY);
        for (int i = 0; i < MAX_COUNT_CURRENCY && startIndex + i < allCurrencies.size(); i++) {
            batch.add(allCurrencies.get(startIndex + i));
        }
        return batch;
    }

    private String buildUrl(List<Currency> batch) {
        String[] currencyCodes = new String[MAX_COUNT_CURRENCY];
        for (int i = 0; i < MAX_COUNT_CURRENCY; i++) {
            currencyCodes[i] = (i < batch.size()) ? batch.get(i).name() : "";
        }
        return String.format(agentConfig.getUrl(),
                currencyCodes[0], currencyCodes[1], currencyCodes[2],
                currencyCodes[3], currencyCodes[4]);
    }

    private Price fetchCurrencyPrice(Currency currency) {
        try {
            BigDecimal buyPrice = getCurrencyPrice(currency, CurrentPrice.BUY);
            BigDecimal sellPrice = getCurrencyPrice(currency, CurrentPrice.SELL);

            if (buyPrice != null && sellPrice != null) {
                return Price.createWithCurrentTime(
                        Banks.SBER,
                        currency.getDisplayName(),
                        buyPrice,
                        sellPrice
                );
            }
            return null;
        } catch (TimeoutException e) {
            log.warn("Timeout fetching price for {}: {}", currency.name(), e.getMessage());
            return null;
        } catch (NoSuchElementException e) {
            log.warn("Element not found for {}: {}", currency.name(), e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("Unexpected error fetching price for {}: {}", currency.name(), e.getMessage());
            return null;
        }
    }

    private BigDecimal getCurrencyPrice(Currency currency, CurrentPrice currentPrice) {
        Optional<WebElement> webElementOpt = getWebElement(currency, currentPrice);
        if (webElementOpt.isEmpty()) {
            log.debug("WebElement not found for {} {}", currency.name(), currentPrice);
            return null;
        }
        WebElement webElement = webElementOpt.get();
        String text = webElement.getText();
        if (text == null || text.isBlank()) {
            log.debug("Empty text for {} {}", currency.name(), currentPrice);
            return null;
        }
        try {
            String clean = text.replaceAll("[^\\d,\\.]", "").replace(',', '.');
            return new BigDecimal(clean);
        } catch (NumberFormatException e) {
            log.warn("Invalid price format for {} {}: '{}'",
                    currency.name(), currentPrice, text);
            return null;
        }
    }

    private Optional<WebElement> getWebElement(Currency currency, CurrentPrice currentPrice) {
        String index = switch (currentPrice) {
            case BUY -> agentConfig.getIndexBuy();
            case SELL -> agentConfig.getIndexSell();
        };
        try {
            WebElement element = webDriverSupport.getWebDriver()
                    .until(ExpectedConditions.presenceOfElementLocated(
                            By.xpath(String.format(agentConfig.getWebElement(),
                                    currency.getDisplayName(), currency.name(), index))
                    ));
            return Optional.of(element);
        } catch (TimeoutException e) {
            log.debug("Primary XPath failed for {} {}, trying alternative...",
                    currency.name(), currentPrice);
            return tryAlternativeXPath(currency, index);
        }
    }

    private Optional<WebElement> tryAlternativeXPath(Currency currency, String index) {
        try {
            String alternativeXPath = String.format(
                    "//div[contains(@class, 'rates') or contains(@class, 'currency')]" +
                            "//div[contains(., '%s')]" +
                            "//div[contains(@class, 'price') or contains(@class, 'value')][%s]",
                    currency.name(), index
            );
            WebElement element = webDriverSupport.getWebDriver()
                    .until(ExpectedConditions.presenceOfElementLocated(
                            By.xpath(alternativeXPath)
                    ));
            return Optional.of(element);
        } catch (TimeoutException e) {
            log.debug("Alternative XPath also failed for {} {}", currency.name(), index);
            return Optional.empty();
        }
    }
}