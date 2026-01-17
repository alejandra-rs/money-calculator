package software.ulpgc.moneycalculator.application.custom.beatles;

import software.ulpgc.moneycalculator.application.custom.Desktop;
import software.ulpgc.moneycalculator.application.webservice.WebServiceExchangeRateSeriesStore;
import software.ulpgc.moneycalculator.application.webservice.WebServiceCurrencyStore;
import software.ulpgc.moneycalculator.application.webservice.WebServiceExchangeRateStore;
import software.ulpgc.moneycalculator.architecture.control.ExchangeMoneyCommand;
import software.ulpgc.moneycalculator.architecture.control.SwapCurrenciesCommand;
import software.ulpgc.moneycalculator.architecture.control.ViewHistoryCommand;

public class Main {
    public static void main(String[] args) throws Exception {

        Desktop desktop = Desktop.with(WebServiceCurrencyStore.forCurrentCurrencies().currencies(),
                                       WebServiceCurrencyStore.forHistoricalCurrencies().currencies());

        desktop.addCommand("exchange", exchangeMoneyCommand(desktop))
                .addCommand("historicalExchange", historicalExchangeMoneyCommand(desktop))
                .addCommand("generateGraphics", viewHistoryCommand(desktop))
                .addCommand("swapCurrencies", swapCurrenciesCommand(desktop))
                .generateUi().setVisible(true);

    }

    private static ExchangeMoneyCommand exchangeMoneyCommand(Desktop desktop) {
        return new ExchangeMoneyCommand(
                desktop.uiElementFactory().exchangeMoneyDialog(),
                new WebServiceExchangeRateStore()
        );
    }

    private static ExchangeMoneyCommand historicalExchangeMoneyCommand(Desktop desktop) {
        return new ExchangeMoneyCommand(
                desktop.uiElementFactory().historicalExchangeMoneyDialog(),
                new WebServiceExchangeRateStore()
        );
    }

    private static ViewHistoryCommand viewHistoryCommand(Desktop desktop) {
        return new ViewHistoryCommand(
                desktop.uiElementFactory().exchangeCurrencyDialog(),
                new WebServiceExchangeRateSeriesStore()
        );
    }

    private static SwapCurrenciesCommand swapCurrenciesCommand(Desktop desktop) {
        return new SwapCurrenciesCommand(desktop.uiElementFactory().currencyPanel());
    }
}