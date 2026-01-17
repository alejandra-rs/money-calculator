package software.ulpgc.moneycalculator.architecture.view;

import software.ulpgc.moneycalculator.architecture.io.CurrencyStore;
import software.ulpgc.moneycalculator.architecture.model.Currency;

import java.util.HashMap;
import java.util.Map;

public class CurrencyQuery {

    private final CurrencyStore store;
    private final Map<String, Currency> currencyMap;

    public CurrencyQuery(CurrencyStore store) {
        this.store = store;
        this.currencyMap = new HashMap<>();
    }

    public Currency[] all() {
        return store.currencies().toArray(Currency[]::new);
    }

    public Currency currencyWith(String code) {
        return currencyMap.computeIfAbsent(code, this::findCurrencyWith);
    }

    public Currency euro() {
        return currencyWith("EUR");
    }

    private Currency findCurrencyWith(String code) {
        return store.currencies()
                .filter(c -> c.code().equals(code))
                .findFirst().orElse(Currency.Null);
    }

}
