package software.ulpgc.moneycalculator.application.custom;

import software.ulpgc.moneycalculator.architecture.model.ExchangeRate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Database {

    public static class ExchangeRateMerger {
        public static ExchangeRate mergeExchangeRates(ExchangeRate rate1, ExchangeRate rate2) {
            return new ExchangeRate(
                    rate1.date(),
                    rate1.to(),
                    rate2.to(),
                    rate2.rate() / rate1.rate()
            );
        }
    }
    public static class DateParser {
        public static LocalDate dateOf(String date) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            return LocalDate.parse(date, formatter);
        }
    }
}
