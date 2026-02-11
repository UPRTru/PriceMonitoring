package prices.agent;

import com.precious.shared.dto.Price;

import java.util.HashMap;

public interface Agent {

    HashMap<String, Price> getPrices();
}
