package prices.agent;

import shared.dto.Price;

import java.util.Map;

public interface Agent {

    Map<String, Price> getPrices();

    String getName();
}