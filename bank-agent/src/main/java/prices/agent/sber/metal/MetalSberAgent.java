package prices.agent.sber.metal;

import shared.dto.Price;
import shared.enums.Banks;
import shared.enums.CurrentPrice;
import shared.enums.Metal;
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
import java.util.HashMap;
import java.util.Map;

@Component(MetalSberAgent.AGENT_NAME)
public final class MetalSberAgent implements Agent {

    public static final String AGENT_NAME = "sber agent metal";

    private final AgentConfig agentConfig;
    private final WebDriverSupport webDriverSupport;

    public MetalSberAgent(@Qualifier("webDriverSupport") WebDriverSupport webDriverSupport) {
        this.agentConfig = EnumAgentsConfig.SBER_METAL.getAgentConfig();
        this.webDriverSupport = webDriverSupport;
    }


    @Override
    public Map<String, Price> getPrices() {
        webDriverSupport.createDriver();
        try {
            webDriverSupport.goToPage(agentConfig.getUrl());
            return getMetalsPrices();
        } finally {
            webDriverSupport.close();
        }
    }

    @Override
    public String getName() {
        return AGENT_NAME;
    }

    private Map<String, Price> getMetalsPrices() {
        Map<String, Price> result = new HashMap<>(Metal.values().length);
        for (Metal metal : Metal.values()) {
            Price price = getMetalPrice(metal);
            result.put(metal.name(), price);
        }
        return result;
    }

    private Price getMetalPrice(Metal metal) {
        return Price.createWithCurrentTime(
                Banks.SBER,
                metal.getDisplayName(),
                getMetalPriceValue(metal, CurrentPrice.BUY),
                getMetalPriceValue(metal, CurrentPrice.SELL)
        );
    }

    private BigDecimal getMetalPriceValue(Metal metal, CurrentPrice currentPrice) {
        WebElement webElement = getWebElement(metal, currentPrice);
        String clean = webElement.getText().replaceAll("[^\\d,\\.]", "").replace(',', '.');
        return new BigDecimal(clean);
    }

    private WebElement getWebElement(Metal metal, CurrentPrice currentPrice) {
        String index = switch (currentPrice) {
            case BUY -> agentConfig.getIndexBuy();
            case SELL -> agentConfig.getIndexSell();
        };
        return webDriverSupport.getWebDriver().until(
                ExpectedConditions.presenceOfElementLocated(
                        By.xpath(String.format(agentConfig.getWebElement(),
                                metal.getDisplayName(), index))
                )
        );
    }
}