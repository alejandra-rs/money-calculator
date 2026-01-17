package software.ulpgc.moneycalculator.application.database;

import software.ulpgc.moneycalculator.architecture.io.CurrencyStore;
import software.ulpgc.moneycalculator.architecture.model.Currency;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.stream.Stream;

public class DatabaseCurrencyStore implements CurrencyStore {

    private final Connection connection;
    private final String tableName;

    public DatabaseCurrencyStore(Connection connection, String tableName) {
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
