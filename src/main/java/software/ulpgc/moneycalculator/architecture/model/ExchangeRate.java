package software.ulpgc.moneycalculator.architecture.model;

import java.time.LocalDate;

public record ExchangeRate(LocalDate date, Currency from, Currency to, double rate) {

    public static final ExchangeRate Null = new ExchangeRate(LocalDate.MIN,
            Currency.Null,
            Currency.Null,
            0.0);
}
