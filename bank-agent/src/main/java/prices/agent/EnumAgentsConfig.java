package prices.agent;

import prices.agent.sber.currency.SberAgentCurrencyConfig;
import prices.agent.sber.metal.SberAgentMetalConfig;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    private static final Map<String, EnumAgentsConfig> BY_AGENT_NAME = Stream.of(values())
            .collect(Collectors.toUnmodifiableMap(
                    config -> config.getAgentConfig().getClass().getSimpleName(),
                    Function.identity()
            ));

    public static java.util.Optional<EnumAgentsConfig> findByAgentName(String agentName) {
        if (agentName == null || agentName.isBlank()) {
            return java.util.Optional.empty();
        }
        return java.util.Optional.ofNullable(BY_AGENT_NAME.get(agentName));
    }
}