package prices.agent.sber.metal;

import com.precious.shared.dto.Price;
import com.precious.shared.enums.Banks;
import com.precious.shared.enums.CurrentPrice;
import com.precious.shared.enums.Metal;
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
import java.util.HashMap;

@Component(MetalSberAgent.AGENT_NAME)
public final class MetalSberAgent implements Agent {

    public static final String AGENT_NAME = "sber agent metal";

    private final AgentConfig agentConfig;
    private final WebDriverSupport webDriverSupport;

    public MetalSberAgent() {
        this.agentConfig = EnumAgentsConfig.SBER_METAL.getAgentConfig();
        this.webDriverSupport = new WebDriverSupport();
    }

    @Override
    public HashMap<String, Price> getPrices() {
        webDriverSupport.createDriver();
        webDriverSupport.goToPage(agentConfig.getUrl());
        HashMap<String, Price> result = getMetalsPrices();
        webDriverSupport.closeDriver();
        return result;
    }

    private HashMap<String, Price> getMetalsPrices() {
        HashMap<String, Price> result = new HashMap<>();
        for (Metal metal : Metal.values()) {
            Price price = getMetalPrices(metal);
            result.put(price.name(), price);
        }
        return result;
    }

    private Price getMetalPrices(Metal metal) {
        return new Price(Banks.SBER,
                metal.getDisplayName(),
                getJsonMetalPrice(metal, CurrentPrice.BUY),
                getJsonMetalPrice(metal, CurrentPrice.SELL),
                Instant.now().toEpochMilli());
    }

    private BigDecimal getJsonMetalPrice(Metal metal, CurrentPrice currentPrice) {
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
                        By.xpath(String.format(agentConfig.getWebElement(), metal.getDisplayName(), index))
                )
        );
    }
}