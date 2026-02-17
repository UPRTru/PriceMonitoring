package prices.agent.sber.currency;

import com.precious.shared.dto.Price;
import com.precious.shared.enums.Banks;
import com.precious.shared.enums.Currency;
import com.precious.shared.enums.CurrentPrice;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.springframework.stereotype.Component;
import prices.agent.Agent;
import prices.agent.AgentConfig;
import prices.agent.EnumAgentsConfig;
import prices.agent.WebDriverSupport;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component(CurrencySberAgent.AGENT_NAME)
public final class CurrencySberAgent implements Agent {

    public static final String AGENT_NAME = "sber agent current";
    private static final int MAX_COUNT_CURRENCY = 5;

    private final AgentConfig agentConfig;
    private final WebDriverSupport webDriverSupport;

    public CurrencySberAgent() {
        this.agentConfig = EnumAgentsConfig.SBER_CURRENT.getAgentConfig();
        this.webDriverSupport = new WebDriverSupport();
    }

    @Override
    public HashMap<String, Price> getPrices() {
        webDriverSupport.createDriver();
        int iterations = getCountIterations(Currency.getCurrencyByBanks(Banks.SBER));
        int index = 0;
        HashMap<String, Price> result = new HashMap<>(Currency.values().length);
        while (iterations > 0) {
            List<Currency> currencies = new ArrayList<>(MAX_COUNT_CURRENCY);
            String[] currencyNames = new String[MAX_COUNT_CURRENCY];
            for (int i = 0; i < MAX_COUNT_CURRENCY; i++) {
                Currency currency = Currency.values()[index];
                currencyNames[i] = currency.name();
                currencies.add(currency);
                index++;
            }
            String url = String.format(agentConfig.getUrl(),
                    currencyNames[0],
                    currencyNames[1],
                    currencyNames[2],
                    currencyNames[3],
                    currencyNames[4]);
            webDriverSupport.goToPage(url);
            currencies.forEach(currency -> {
                result.put(
                        currency.name(),
                        new Price(Banks.SBER,
                                currency.getDisplayName(),
                                getCurrencyPrice(currency, CurrentPrice.BUY),
                                getCurrencyPrice(currency, CurrentPrice.SELL),
                                Instant.now().toEpochMilli())
                );
            });
            iterations--;
        }
        webDriverSupport.closeDriver();
        return result;
    }

    private int getCountIterations(List<Currency> allCurrenciesByBank) {
        if (allCurrenciesByBank.isEmpty()) {
            return 0;
        } else if (allCurrenciesByBank.size() <= MAX_COUNT_CURRENCY) {
            return 1;
        } else {
            return allCurrenciesByBank.size() / MAX_COUNT_CURRENCY;
        }
    }

    private BigDecimal getCurrencyPrice(Currency currency, CurrentPrice currentPrice) {
        WebElement webElement = getWebElement(currency, currentPrice);
        String clean = webElement.getText().replaceAll("[^\\d,\\.]", "").replace(',', '.');
        return new BigDecimal(clean);
    }

    private WebElement getWebElement(Currency currency, CurrentPrice currentPrice) {
        String index = switch (currentPrice) {
            case BUY -> agentConfig.getIndexBuy();
            case SELL -> agentConfig.getIndexSell();
        };
        return webDriverSupport.getWebDriver().until(
                ExpectedConditions.presenceOfElementLocated(
                        By.xpath(String.format(agentConfig.getWebElement(), currency.getDisplayName(), currency.name(), index))
                )
        );
    }
}