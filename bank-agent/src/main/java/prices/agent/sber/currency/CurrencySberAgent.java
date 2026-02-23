package prices.agent.sber.currency;

import shared.dto.Price;
import shared.enums.Banks;
import shared.enums.Currency;
import shared.enums.CurrentPrice;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import prices.agent.Agent;
import prices.agent.AgentConfig;
import prices.agent.EnumAgentsConfig;
import prices.agent.WebDriverSupport;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component(CurrencySberAgent.AGENT_NAME)
public final class CurrencySberAgent implements Agent {

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
            int iterations = calculateIterations(currenciesForBank.size());
            int index = 0;
            Map<String, Price> result = new HashMap<>(currenciesForBank.size());

            while (iterations > 0) {
                List<Currency> batch = getNextBatch(currenciesForBank, index);
                String url = buildUrl(batch);
                webDriverSupport.goToPage(url);

                for (Currency currency : batch) {
                    Price price = Price.createWithCurrentTime(
                            Banks.SBER,
                            currency.getDisplayName(),
                            getCurrencyPrice(currency, CurrentPrice.BUY),
                            getCurrencyPrice(currency, CurrentPrice.SELL)
                    );
                    result.put(currency.name(), price);
                }
                index += MAX_COUNT_CURRENCY;
                iterations--;
            }
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
                        By.xpath(String.format(agentConfig.getWebElement(),
                                currency.getDisplayName(), currency.name(), index))
                )
        );
    }
}