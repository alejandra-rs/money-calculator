package software.ulpgc.moneycalculator.application.beatles;

import software.ulpgc.moneycalculator.architecture.model.Currency;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.stream.Stream;

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

}
