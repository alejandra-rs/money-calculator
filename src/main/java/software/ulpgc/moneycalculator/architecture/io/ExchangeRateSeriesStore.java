package software.ulpgc.moneycalculator.architecture.io;

import software.ulpgc.moneycalculator.architecture.model.ExchangeRate;
import software.ulpgc.moneycalculator.architecture.model.Currency;

import java.time.LocalDate;
import java.util.stream.Stream;

public interface ExchangeRateSeriesStore {
    Stream<ExchangeRate> exchangeRatesBetween(Currency from, Currency to, LocalDate start, LocalDate end);
}