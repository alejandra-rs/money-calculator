package software.ulpgc.moneycalculator.architecture.io;

import software.ulpgc.moneycalculator.architecture.model.Currency;
import software.ulpgc.moneycalculator.architecture.model.ExchangeRate;

import java.time.LocalDate;

public interface ExchangeRateStore {
    ExchangeRate load(Currency from, Currency to, LocalDate date);
}
