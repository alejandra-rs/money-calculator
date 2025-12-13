package software.ulpgc.moneycalculator.application.beatles.test;

import software.ulpgc.moneycalculator.application.beatles.Desktop;
import software.ulpgc.moneycalculator.application.beatles.WebService;
import software.ulpgc.moneycalculator.architecture.control.ExchangeMoneyCommand;
import software.ulpgc.moneycalculator.architecture.control.HistoricalExchangeMoneyCommand;
import software.ulpgc.moneycalculator.architecture.control.ViewHistoryCommand;

public class Main {
    public static void main(String[] args) throws Exception {

        Desktop desktop = Desktop.with(WebService.CurrencyStore.forCurrentCurrencies().currencies(),
                                       WebService.CurrencyStore.forHistoricalCurrencies().currencies());

        desktop.addCommand("exchange", new ExchangeMoneyCommand(
                desktop.uiElementFactory().moneyDialog(),
                desktop.uiElementFactory().outputCurrencyDialog(),
                new WebService.ExchangeRateStore(),
                desktop.uiElementFactory().moneyDisplay()
        ))
        .addCommand("historicalExchange", new HistoricalExchangeMoneyCommand(
                desktop.uiElementFactory().moneyDialog(),
                desktop.uiElementFactory().outputCurrencyDialog(),
                desktop.uiElementFactory().inputDateDialog(),
                new WebService.ExchangeRateStore(),
                desktop.uiElementFactory().moneyDisplay()
        ))
        .addCommand("generateGraphics", new ViewHistoryCommand(
                desktop.uiElementFactory().inputStartDateDialog(),
                desktop.uiElementFactory().inputEndDateDialog(),
                desktop.uiElementFactory().inputCurrencyDialog(),
                desktop.uiElementFactory().outputCurrencyDialog(),
                desktop.uiElementFactory().lineChartDisplay(),
                new WebService.ExchangeRateSeriesStore()
        ))
        .generateUi().setVisible(true);
    }
}