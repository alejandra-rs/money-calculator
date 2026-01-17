package software.ulpgc.moneycalculator.application.database;

import software.ulpgc.moneycalculator.architecture.io.CurrencyStore;
import software.ulpgc.moneycalculator.architecture.io.ExchangeRateStore;
import software.ulpgc.moneycalculator.architecture.model.Currency;
import software.ulpgc.moneycalculator.architecture.model.ExchangeRate;
import software.ulpgc.moneycalculator.architecture.view.CurrencyQuery;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import static software.ulpgc.moneycalculator.application.custom.Database.DateParser.dateOf;
import static software.ulpgc.moneycalculator.application.custom.Database.ExchangeRateMerger.mergeExchangeRates;

public class DatabaseExchangeRateStore implements ExchangeRateStore {

    private final Connection connection;
    private final PreparedStatement preparedStatement;
    private final CurrencyQuery query;

    public DatabaseExchangeRateStore(Connection connection, CurrencyStore store) throws SQLException {
        this.connection = connection;
        this.preparedStatement = connection.prepareStatement("SELECT * FROM exchangeRates WHERE date = ? AND toCurrency = ?");
        this.query = new CurrencyQuery(store);
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
                query.euro(),
                query.currencyWith(resultSet.getString(2)),
                resultSet.getDouble(3)
        );
    }
}
