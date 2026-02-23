package prices.agent.sber.currency;

import prices.agent.AgentConfig;
import prices.agent.sber.metal.SberAgentMetalConfig;

public enum SberAgentCurrencyConfig implements AgentConfig {
    SBER_AGENT_CURRENCY(CurrencySberAgent.AGENT_NAME),
    URL("https://www.sberbank.ru/ru/quotes/currencies?tab=sbol" +
            "&currency=%s" +
            "&currency=%s" +
            "&currency=%s" +
            "&currency=%s" +
            "&currency=%s" +
            "&package=ERNP-2"),
    WEB_ELEMENT("//div[starts-with(@class, 'TabContainer') and not(contains(substring-after(@class, 'TabContainer'), ' '))]"
            + "//div[contains(@class, 'rates-form-new-table-row')]"
            + "[contains(., '%s') or contains(., '%s')]"
            + "//div[contains(@class, 'rates-form-new-table-row__col-wrap')]"
            + "//div[%s]"
            + "//div[contains(@class, 'dk-sbol-text') and contains(text(), '₽')]"),
    INDEX_BUY("1"),
    INDEX_SELL("2");

    private final String config;

    SberAgentCurrencyConfig(String config) {
        this.config = config;
    }

    public String getConfig() {
        return config;
    }

    @Override
    public String getUrl() {
        return URL.getConfig();
    }

    @Override
    public String getWebElement() {
        return WEB_ELEMENT.getConfig();
    }

    @Override
    public String getIndexBuy() {
        return INDEX_BUY.getConfig();
    }

    @Override
    public String getIndexSell() {
        return INDEX_SELL.getConfig();
    }
}