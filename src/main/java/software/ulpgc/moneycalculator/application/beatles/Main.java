package software.ulpgc.moneycalculator.application.beatles;

import software.ulpgc.moneycalculator.architecture.control.ExchangeMoneyCommand;
import software.ulpgc.moneycalculator.architecture.control.HistoricalExchangeMoneyCommand;
import software.ulpgc.moneycalculator.architecture.control.ViewHistoryCommand;
import software.ulpgc.moneycalculator.architecture.io.CurrencyStore;
import software.ulpgc.moneycalculator.architecture.model.Currency;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.stream.Stream;

public class Main {

    private static final String databaseConnectionUrl = "jdbc:sqlite:";
    private static final String currencyDatabase = "currencies.db";

    private static final String currenciesTable = "currencies";
    private static final String historicalCurrenciesTable = "historicalCurrencies";

    private static Connection connection;

    public static void main(String[] args) throws SQLException {

        connection = DriverManager.getConnection(databaseConnectionUrl + currencyDatabase);
        connection.setAutoCommit(false);

        Desktop desktop = new Desktop(currenciesIn(currenciesTable).currencies(),
                                      currenciesIn(historicalCurrenciesTable).currencies());

        desktop.addCommand("exchange", new ExchangeMoneyCommand(
                desktop.moneyDialog(),
                desktop.outputCurrencyDialog(),
                new WebService.ExchangeRateStore(),
                desktop.moneyDisplay()
        ));
        desktop.addCommand("historicalExchange", new HistoricalExchangeMoneyCommand(
                desktop.moneyDialog(),
                desktop.outputCurrencyDialog(),
                desktop.inputDateDialog(),
                new WebService.HistoricalExchangeRateStore(),
                desktop.moneyDisplay()
        ));
        desktop.addCommand("viewHistory", new ViewHistoryCommand(
                desktop.inputStartDateDialog(),
                desktop.inputEndDateDialog(),
                desktop.inputCurrencyDialog(),
                desktop.outputCurrencyDialog(),
                desktop.lineChartDisplay(),
                new WebService.ExchangeRateSeriesStore()
        ));
        desktop.setVisible(true);
    }

    private static CurrencyStore currenciesIn(String tableName) throws SQLException {
        if (tableNonExistent(connection, tableName)) importCurrenciesInto(connection, tableName);
        return new Database.CurrencyStore(connection, tableName);
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

    private static void importCurrenciesInto(Connection connection, String tableName) throws SQLException {
        Stream<Currency> currencies = tableName.equals(currenciesTable) ?
                new WebService.CurrencyStore().currencies() :
                new WebService.HistoricalCurrencyStore().currencies();
        new Database.CurrencyRecorder(connection, tableName).record(currencies);
    }

}
