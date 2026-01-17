package software.ulpgc.moneycalculator.application.database;

import software.ulpgc.moneycalculator.architecture.io.ExchangeRateRecorder;
import software.ulpgc.moneycalculator.architecture.model.ExchangeRate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.stream.Stream;

public class DatabaseExchangeRateRecorder implements ExchangeRateRecorder {

    private final Connection connection;
    private final PreparedStatement preparedStatement;

    public DatabaseExchangeRateRecorder(Connection connection) throws SQLException {
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
