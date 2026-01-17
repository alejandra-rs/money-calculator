package software.ulpgc.moneycalculator.application.database;

import software.ulpgc.moneycalculator.application.custom.Database;
import software.ulpgc.moneycalculator.architecture.io.ExchangeRateSeriesStore;
import software.ulpgc.moneycalculator.architecture.model.Currency;
import software.ulpgc.moneycalculator.architecture.model.ExchangeRate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Objects;
import java.util.stream.Stream;

import static software.ulpgc.moneycalculator.application.custom.Database.ExchangeRateMerger.mergeExchangeRates;

public class DatabaseExchangeRateSeriesStore implements ExchangeRateSeriesStore {

    private final Connection connection;
    private final String preparedStatement;
    private final Database.ExchangeRateReader reader;

    public DatabaseExchangeRateSeriesStore(Connection connection, software.ulpgc.moneycalculator.architecture.io.CurrencyStore store) {
        this.connection = connection;
        this.preparedStatement = "SELECT * FROM exchangeRates WHERE toCurrency = ? AND date BETWEEN ? AND ?";
        this.reader = new Database.ExchangeRateReader(store);
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
            return mergeExchangeRates(reader.readExchangeRateIn(fromRs), reader.readExchangeRateIn(toRs));
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
