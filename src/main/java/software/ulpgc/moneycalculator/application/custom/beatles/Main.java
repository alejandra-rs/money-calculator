package software.ulpgc.moneycalculator.application.custom.beatles;

import software.ulpgc.moneycalculator.application.custom.Desktop;
import software.ulpgc.moneycalculator.application.webservice.WebServiceExchangeRateSeriesStore;
import software.ulpgc.moneycalculator.application.webservice.WebServiceCurrencyStore;
import software.ulpgc.moneycalculator.application.webservice.WebServiceExchangeRateStore;
import software.ulpgc.moneycalculator.architecture.control.ExchangeMoneyCommand;
import software.ulpgc.moneycalculator.architecture.control.HistoricalExchangeMoneyCommand;
import software.ulpgc.moneycalculator.architecture.control.ViewHistoryCommand;

public class Main {
    public static void main(String[] args) throws Exception {

        Desktop desktop = Desktop.with(WebServiceCurrencyStore.forCurrentCurrencies().currencies(),
                                       WebServiceCurrencyStore.forHistoricalCurrencies().currencies());

        desktop.addCommand("exchange", exchangeMoneyCommand(desktop))
                .addCommand("historicalExchange", historicalExchangeMoneyCommand(desktop))
                .addCommand("generateGraphics", viewHistoryCommand(desktop))
                .generateUi().setVisible(true);

    }

    private static ExchangeMoneyCommand exchangeMoneyCommand(Desktop desktop) {
        return new ExchangeMoneyCommand(
                desktop.uiElementFactory().moneyDialog(),
                desktop.uiElementFactory().outputCurrencyDialog(),
                new WebServiceExchangeRateStore(),
                desktop.uiElementFactory().moneyDisplay()
        );
    }

    private static HistoricalExchangeMoneyCommand historicalExchangeMoneyCommand(Desktop desktop) {
        return new HistoricalExchangeMoneyCommand(
                desktop.uiElementFactory().moneyDialog(),
                desktop.uiElementFactory().outputCurrencyDialog(),
                desktop.uiElementFactory().inputDateDialog(),
                new WebServiceExchangeRateStore(),
                desktop.uiElementFactory().moneyDisplay()
        );
    }

    private static ViewHistoryCommand viewHistoryCommand(Desktop desktop) {
        return new ViewHistoryCommand(
                desktop.uiElementFactory().inputStartDateDialog(),
                desktop.uiElementFactory().inputEndDateDialog(),
                desktop.uiElementFactory().inputCurrencyDialog(),
                desktop.uiElementFactory().outputCurrencyDialog(),
                desktop.uiElementFactory().lineChartDisplay(),
                new WebServiceExchangeRateSeriesStore()
        );
    }
}