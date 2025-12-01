package software.ulpgc.moneycalculator.application.beatles;

import software.ulpgc.moneycalculator.architecture.model.Currency;
import software.ulpgc.moneycalculator.architecture.model.ExchangeRate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

public class Database {

    public static class CurrencyRecorder implements software.ulpgc.moneycalculator.architecture.io.CurrencyRecorder {

        private final Connection connection;
        private final PreparedStatement preparedStatement;

        public CurrencyRecorder(Connection connection, String tableName) throws SQLException {
            this.connection = connection;
            createTableIfNotExists(tableName);
            this.preparedStatement = connection.prepareStatement("INSERT INTO " + tableName + " (code, country) VALUES (?, ?)");
        }

        @Override
        public void record(Stream<Currency> currencies) {
            try {
                currencies.forEach(this::insert);
                preparedStatement.executeBatch();
                connection.commit();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        private void insert(Currency currency) {
            try {
                preparedStatement.setString(1, currency.code());
                preparedStatement.setString(2, currency.country());
                preparedStatement.addBatch();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        private void createTableIfNotExists(String tableName) throws SQLException {
            this.connection.createStatement().execute("CREATE TABLE IF NOT EXISTS " + tableName + " (code TEXT, country TEXT)");
        }
    }

    public static class CurrencyStore implements software.ulpgc.moneycalculator.architecture.io.CurrencyStore {

        private final Connection connection;
        private final String tableName;

        public CurrencyStore(Connection connection, String tableName) {
            this.connection = connection;
            this.tableName = tableName;
        }

        @Override
        public Stream<Currency> currencies() {
            try {
                return currenciesIn(resultSet(tableName));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        private Stream<Currency> currenciesIn(ResultSet resultSet) {
            return Stream.generate(() -> nextCurrencyIn(resultSet))
                    .onClose(() -> close(resultSet))
                    .takeWhile(Objects::nonNull);
        }

        private Currency nextCurrencyIn(ResultSet resultSet) {
            try {
                return resultSet.next() ? readCurrencyIn(resultSet) : null;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        private Currency readCurrencyIn(ResultSet resultSet) {
            try {
                return new Currency(resultSet.getString(1), resultSet.getString(2));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        private void close(ResultSet resultSet) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        private ResultSet resultSet(String tableName) throws SQLException {
            return this.connection.createStatement().executeQuery("SELECT * FROM " + tableName + " ORDER BY code");
        }

    }

    public static class ExchangeRateRecorder implements software.ulpgc.moneycalculator.architecture.io.ExchangeRateRecorder {

        private final Connection connection;
        private final PreparedStatement preparedStatement;

        public ExchangeRateRecorder(Connection connection) throws SQLException {
            this.connection = connection;
            createTableIfNotExists();
            this.preparedStatement = connection.prepareStatement("INSERT OR IGNORE INTO exchangeRates (date, toCurrency, rate) VALUES (?, ?, ?)");
        }

        @Override
        public void record(Stream<ExchangeRate> exchangeRates) {
            try {
                exchangeRates.forEach(this::record);
                flushBatch();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        private void flushBatch() throws SQLException {
            preparedStatement.executeBatch();
            connection.commit();
        }

        private void record(ExchangeRate exchangeRate) {
            try {
                insert(exchangeRate);
                flushBatchIfRequired();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        private int count = 0;
        private void flushBatchIfRequired() throws SQLException {
            if (++count % 10000 == 0) preparedStatement.executeBatch();
        }

        private void insert(ExchangeRate exchangeRate) {
            try {
                preparedStatement.setString(1, exchangeRate.date().toString());
                preparedStatement.setString(2, exchangeRate.to().code());
                preparedStatement.setDouble(3, exchangeRate.rate());
                preparedStatement.addBatch();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        private void createTableIfNotExists() throws SQLException {
            this.connection.createStatement().execute("CREATE TABLE IF NOT EXISTS exchangeRates (date TEXT, toCurrency TEXT, rate REAL, PRIMARY KEY (date, toCurrency))");
        }
    }

    public static class ExchangeRateStore implements software.ulpgc.moneycalculator.architecture.io.ExchangeRateStore {

        private final Connection connection;
        private final PreparedStatement preparedStatement;
        private final Map<String, Currency> currencies;

        public ExchangeRateStore(Connection connection, software.ulpgc.moneycalculator.architecture.io.CurrencyStore store) throws SQLException {
            this.connection = connection;
            this.preparedStatement = connection.prepareStatement("SELECT * FROM exchangeRates WHERE date = ? AND toCurrency = ?");
            this.currencies = store.currencies().collect(toMap(Currency::code, c -> c));
        }

        @Override
        public ExchangeRate load(Currency from, Currency to, LocalDate date) {
            try {
                return mergeExchangeRates(exchangeRateFromEuroTo(from, date), exchangeRateFromEuroTo(to, date));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        public LocalDate latestStoredDate() throws SQLException {
            return dateOf(maxDateQuery());
        }

        private String maxDateQuery() throws SQLException {
            return resultSet().getString(1);
        }

        private ResultSet resultSet() throws SQLException {
            return connection.createStatement().executeQuery("SELECT MAX(date) FROM exchangeRates");
        }

        public static ExchangeRate mergeExchangeRates(ExchangeRate rate1, ExchangeRate rate2) {
            return new ExchangeRate(
                    rate1.date(),
                    rate1.to(),
                    rate2.to(),
                    rate2.rate() / rate1.rate()
            );
        }

        private ExchangeRate exchangeRateFromEuroTo(Currency currency, LocalDate date) throws SQLException {
            return exchangeRateIn(resultSet(currency, date));
        }

        private ResultSet resultSet(Currency to, LocalDate date) throws SQLException {
            preparedStatement.setString(1, date.toString());
            preparedStatement.setString(2, to.code());
            return preparedStatement.executeQuery();
        }

        private ExchangeRate exchangeRateIn(ResultSet resultSet) {
            try {
                ExchangeRate exchangeRate = resultSet.next() ? readExchangeRateIn(resultSet) : ExchangeRate.Null;
                resultSet.close();
                return exchangeRate;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
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

        public static LocalDate dateOf(String date) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            return LocalDate.parse(date, formatter);
        }
    }

    public static class ExchangeRateSeriesStore implements software.ulpgc.moneycalculator.architecture.io.ExchangeRateSeriesStore {

        private final Connection connection;
        private final String preparedStatement;
        private final Map<String, Currency> currencies;

        public ExchangeRateSeriesStore(Connection connection, software.ulpgc.moneycalculator.architecture.io.CurrencyStore store) {
            this.connection = connection;
            this.preparedStatement = "SELECT * FROM exchangeRates WHERE toCurrency = ? AND date BETWEEN ? AND ?";
            this.currencies = store.currencies().collect(toMap(Currency::code, c -> c));
        }

        @Override
        public Stream<ExchangeRate> exchangeRatesBetween(Currency from, Currency to, LocalDate start, LocalDate end) {
            try {
                return zipExchangeRatesFrom(preparedStatement(from, start, end), preparedStatement(to, start, end));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        private Stream<ExchangeRate> zipExchangeRatesFrom(PreparedStatement fromStatement, PreparedStatement toStatement) throws SQLException {
            return zipExchangeRatesFrom(resultSetIn(fromStatement), resultSetIn(toStatement))
                    .onClose(() -> close(fromStatement, toStatement));
        }

        private Stream<ExchangeRate> zipExchangeRatesFrom(ResultSet fromRs, ResultSet toRs) {
            return Stream.generate(() -> nextCompoundRateIn(fromRs, toRs))
                    .onClose(() -> close(toRs, fromRs))
                    .takeWhile(Objects::nonNull);
        }

        private ExchangeRate nextCompoundRateIn(ResultSet fromRs, ResultSet toRs) {
            try {
                if (!fromRs.next() || !toRs.next()) return null;
                return mergeExchangeRates(readExchangeRateIn(fromRs), readExchangeRateIn(toRs));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        private ResultSet resultSetIn(PreparedStatement statement) {
            try {
                return statement.executeQuery();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        private PreparedStatement preparedStatement(Currency currency, LocalDate start, LocalDate end) throws SQLException {
            PreparedStatement statement = connection.prepareStatement(preparedStatement);
            statement.setString(1, currency.code());
            statement.setString(2, start.toString());
            statement.setString(3, end.toString());
            return statement;
        }

        public static ExchangeRate mergeExchangeRates(ExchangeRate rate1, ExchangeRate rate2) {
            return new ExchangeRate(
                    rate1.date(),
                    rate1.to(),
                    rate2.to(),
                    rate2.rate() / rate1.rate()
            );
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

        public static LocalDate dateOf(String date) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            return LocalDate.parse(date, formatter);
        }

        private void close(PreparedStatement fromStatement, PreparedStatement toStatement) {
            try {
                fromStatement.close();
                toStatement.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        private void close(ResultSet fromRs, ResultSet toRs) {
            try {
                fromRs.close();
                toRs.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
