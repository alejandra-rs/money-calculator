package software.ulpgc.moneycalculator.application.custom.rollingstones;

import software.ulpgc.moneycalculator.application.custom.Desktop;
import software.ulpgc.moneycalculator.application.webservice.WebServiceExchangeRateSeriesStore;
import software.ulpgc.moneycalculator.application.database.*;
import software.ulpgc.moneycalculator.application.webservice.WebServiceCurrencyStore;
import software.ulpgc.moneycalculator.application.webservice.WebServiceExchangeRateStore;
import software.ulpgc.moneycalculator.architecture.control.ExchangeMoneyCommand;
import software.ulpgc.moneycalculator.architecture.control.ViewHistoryCommand;
import software.ulpgc.moneycalculator.architecture.io.CurrencyStore;
import software.ulpgc.moneycalculator.architecture.io.ExchangeRateStore;
import software.ulpgc.moneycalculator.architecture.model.Currency;
import software.ulpgc.moneycalculator.architecture.model.ExchangeRate;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class Main {

    private static final String databaseConnectionUrl = "jdbc:sqlite:";
    private static final String currencyDatabase = "currencies.db";
    private static final String ratesDatabase = "rates.db";
    
    private static final String currenciesTable = "currencies";
    private static final String historicalCurrenciesTable = "historicalCurrencies";
    private static final String ratesTable = "exchangeRates";

    private static Connection currencyConnection;
    private static Connection ratesConnection;

    private static Currency euro;

    public static void main(String[] args) throws Exception {

        currencyConnection = DriverManager.getConnection(databaseConnectionUrl + currencyDatabase);
        ratesConnection = DriverManager.getConnection(databaseConnectionUrl + ratesDatabase);

        currencyConnection.setAutoCommit(false);
        ratesConnection.setAutoCommit(false);

        euro = currenciesIn(historicalCurrenciesTable).currencies()
                .filter(c -> c.code().equals("EUR"))
                .findFirst().orElse(Currency.Null);

        Desktop desktop = Desktop.with(currenciesIn(currenciesTable).currencies(),
                                       currenciesIn(historicalCurrenciesTable).currencies());

        desktop.addCommand("exchange", exchangeMoneyCommand(desktop))
                .addCommand("historicalExchange", historicalExchangeMoneyCommand(desktop))
                .addCommand("generateGraphics", viewHistoryCommand(desktop))
                .generateUi().setVisible(true);

        Runtime.getRuntime().addShutdownHook(new Thread(Main::closeConnections));
    }

    private static ViewHistoryCommand viewHistoryCommand(Desktop desktop) throws SQLException {
        return new ViewHistoryCommand(
                desktop.uiElementFactory().exchangeCurrencyDialog(),
                new DatabaseExchangeRateSeriesStore(ratesConnection, currenciesIn(currenciesTable))
        );
    }

    private static ExchangeMoneyCommand historicalExchangeMoneyCommand(Desktop desktop) throws SQLException {
        return new ExchangeMoneyCommand(
                desktop.uiElementFactory().historicalExchangeMoneyDialog(),
                ratesIn(ratesConnection)
        );
    }

    private static ExchangeMoneyCommand exchangeMoneyCommand(Desktop desktop) throws SQLException {
        return new ExchangeMoneyCommand(
                desktop.uiElementFactory().exchangeMoneyDialog(),
                ratesIn(ratesConnection)
        );
    }


    private static boolean tableNonExistent(Connection connection, String tableName) throws SQLException {
        return fileIsEmpty(connection) || !connection.getMetaData()
                .getTables(null, null, tableName, new String[]{"TABLE"})
                .next();
    }

    private static boolean fileIsEmpty(Connection connection) throws SQLException {
        return new File(pathOf(connection)).length() == 0;
    }

    private static String pathOf(Connection connection) throws SQLException {
        return pathOf(connection.getMetaData().getURL());
    }

    private static String pathOf(String url) {
        return url.substring(url.lastIndexOf(":") + 1);
    }

    private static CurrencyStore currenciesIn(String tableName) throws SQLException {
        if (tableNonExistent(currencyConnection, tableName)) importCurrenciesInto(currencyConnection, tableName);
        return new DatabaseCurrencyStore(currencyConnection, tableName);
    }

    private static ExchangeRateStore ratesIn(Connection connection) throws SQLException {
        importRatesIfNeeded(connection);
        return new DatabaseExchangeRateStore(connection, currenciesIn(currenciesTable));
    }

    private static void importCurrenciesInto(Connection connection, String tableName) throws SQLException {
        Stream<Currency> currencies = tableName.equals(currenciesTable) ?
                WebServiceCurrencyStore.forCurrentCurrencies().currencies() :
                WebServiceCurrencyStore.forHistoricalCurrencies().currencies();
        new DatabaseCurrencyRecorder(connection, tableName).record(currencies);
    }

    private static void importRatesIfNeeded(Connection connection) throws SQLException {
        if (tableNonExistent(connection, ratesTable)) importAllRatesSince(LocalDate.of(1999, 1, 1));
        importMissingRates();
    }

    private static void importMissingRates() throws SQLException {
        if (lastStoredDate().isEqual(LocalDate.now())) return;
        importAllRatesSince(lastStoredDate());
    }

    private static void importAllRatesSince(LocalDate date) throws SQLException {
        new DatabaseExchangeRateRecorder(ratesConnection).record(Stream.concat(pastRatesSince(date), currentRates()));
    }

    private static Stream<ExchangeRate> pastRatesSince(LocalDate date) throws SQLException {
        return currenciesIn(historicalCurrenciesTable).currencies()
                .flatMap(c -> pastRatesSince(date, c));
    }

    private static Stream<ExchangeRate> pastRatesSince(LocalDate date, Currency c) {
        return c.equals(euro) ? getEuroStream(date) :
                                new WebServiceExchangeRateSeriesStore()
                                        .exchangeRatesBetween(euro, c, date, LocalDate.now().minusDays(1));
    }

    private static Stream<ExchangeRate> getEuroStream(LocalDate date) {
        return LongStream.range(0, LocalDate.now().toEpochDay() - date.toEpochDay())
                .mapToObj(i -> new ExchangeRate(date.plusDays(i), euro, euro, 1.0));
    }

    private static Stream<ExchangeRate> currentRates() throws SQLException {
        return currenciesIn(currenciesTable).currencies().map(c -> currentRates(euro, c));
    }

    private static ExchangeRate currentRates(Currency euro, Currency toCurrency) {
        return new WebServiceExchangeRateStore().load(euro, toCurrency, LocalDate.now());
    }

    private static LocalDate lastStoredDate() throws SQLException {
        return new DatabaseExchangeRateStore(ratesConnection, currenciesIn(currenciesTable)).latestStoredDate();
    }

    private static void closeConnections() {
        try {
            currencyConnection.close();
            ratesConnection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
