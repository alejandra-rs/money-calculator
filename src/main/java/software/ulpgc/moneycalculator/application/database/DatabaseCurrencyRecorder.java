package software.ulpgc.moneycalculator.application.database;

import software.ulpgc.moneycalculator.architecture.io.CurrencyRecorder;
import software.ulpgc.moneycalculator.architecture.model.Currency;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.stream.Stream;

public class DatabaseCurrencyRecorder implements CurrencyRecorder {

    private final Connection connection;
    private final PreparedStatement preparedStatement;

    public DatabaseCurrencyRecorder(Connection connection, String tableName) throws SQLException {
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
