package prices.agent.sber.metal;

import prices.agent.AgentConfig;

public enum SberAgentMetalConfig implements AgentConfig {

    SBER_AGENT_METAL(MetalSberAgent.AGENT_NAME),
    URL("https://www.sberbank.ru/retail/ru/quotes/metalbeznal?tab=online"),
    WEB_ELEMENT("//div[contains(@class, 'rfn-table-currency__iso') " +
            "and text()='%s']" +
            "/ancestor::div[contains(@class, 'rfn-table-row')]" +
            "//div[contains(@class, 'rfn-table-row__price_main')]" +
            "//div[contains(@class, 'rfn-table-row__col')]" +
            "[%s]"),
    INDEX_BUY("3"),
    INDEX_SELL("2");

    private final String config;

    SberAgentMetalConfig(String config) {
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