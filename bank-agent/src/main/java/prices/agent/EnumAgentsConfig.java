package prices.agent;

import prices.agent.sber.currency.SberAgentCurrencyConfig;
import prices.agent.sber.metal.SberAgentMetalConfig;

public enum EnumAgentsConfig {
    SBER_CURRENT(SberAgentCurrencyConfig.SBER_AGENT_CURRENCY),
    SBER_METAL(SberAgentMetalConfig.SBER_AGENT_METAL);

    private final AgentConfig agentConfig;

    EnumAgentsConfig(AgentConfig agentConfig) {
        this.agentConfig = agentConfig;
    }

    public AgentConfig getAgentConfig() {
        return agentConfig;
    }
}
