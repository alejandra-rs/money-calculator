package software.ulpgc.moneycalculator.application.mock;

import software.ulpgc.moneycalculator.architecture.io.CurrencyStore;
import software.ulpgc.moneycalculator.architecture.model.Currency;

import java.util.stream.Stream;

public class MockCurrencyStore implements CurrencyStore {
    @Override
    public Stream<Currency> currencies() {
        return Stream.of(
                new Currency("USD", "USA"),
                new Currency("EUR", "Europa")
        );
    }
}
