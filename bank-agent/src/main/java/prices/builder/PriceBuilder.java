package prices.builder;

import com.precious.shared.dto.Price;
import jakarta.validation.constraints.NotNull;
import prices.model.CurrencyPrice;
import prices.model.MetalPrice;
import prices.model.Priced;

import javax.annotation.Nullable;

public class PriceBuilder {

    public static Price buildPrice(@NotNull Priced priced) {
        return Price.of(priced.getBank(),
                priced.getName(),
                priced.getBuyPrice(),
                priced.getSellPrice(),
                priced.getTimestamp());
    }
}
