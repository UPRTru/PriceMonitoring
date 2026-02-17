package prices.agent;

import com.precious.shared.dto.Price;

import java.util.Map;

public interface Agent {

    Map<String, Price> getPrices();
}
