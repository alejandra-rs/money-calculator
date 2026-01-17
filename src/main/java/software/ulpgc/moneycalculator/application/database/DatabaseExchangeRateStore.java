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

import static java.time.LocalDate.parse;
import static java.time.format.DateTimeFormatter.ofPattern;

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
        return parse(maxDateQuery(), ofPattern("yyyy-MM-dd"));
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
                parse(resultSet.getString(1), ofPattern("yyyy-MM-dd")),
                query.euro(),
                query.currencyWith(resultSet.getString(2)),
                resultSet.getDouble(3)
        );
    }

    public ExchangeRate mergeExchangeRates(ExchangeRate rate1, ExchangeRate rate2) {
        return new ExchangeRate(
                rate1.date(),
                rate1.to(),
                rate2.to(),
                rate2.rate() / rate1.rate()
        );
    }
}
