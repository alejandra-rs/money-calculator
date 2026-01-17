package software.ulpgc.moneycalculator.application.custom;

import software.ulpgc.moneycalculator.architecture.model.Currency;
import software.ulpgc.moneycalculator.architecture.model.ExchangeRate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static java.util.stream.Collectors.toMap;
import static software.ulpgc.moneycalculator.application.custom.Database.DateParser.dateOf;

public class Database {

    public static class ExchangeRateReader {

        private final Map<String, Currency> currencies;

        public ExchangeRateReader(software.ulpgc.moneycalculator.architecture.io.CurrencyStore store) {
            this.currencies = store.currencies().collect(toMap(Currency::code, c -> c));
        }

        public ExchangeRate readExchangeRateIn(ResultSet resultSet) throws SQLException {
            return new ExchangeRate(
                    dateOf(resultSet.getString(1)),
                    currencyOf("EUR"),
                    currencyOf(resultSet.getString(2)),
                    resultSet.getDouble(3)
            );
        }

        private Currency currencyOf(String code) {
            return currencies.getOrDefault(code, Currency.Null);
        }
    }
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
