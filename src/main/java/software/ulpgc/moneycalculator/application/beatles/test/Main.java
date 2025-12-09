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
                desktop.moneyDialog(),
                desktop.outputCurrencyDialog(),
                new WebService.ExchangeRateStore(),
                desktop.moneyDisplay()
        ))
        .addCommand("historicalExchange", new HistoricalExchangeMoneyCommand(
                desktop.moneyDialog(),
                desktop.outputCurrencyDialog(),
                desktop.inputDateDialog(),
                new WebService.ExchangeRateStore(),
                desktop.moneyDisplay()
        ))
        .addCommand("generateGraphics", new ViewHistoryCommand(
                desktop.inputStartDateDialog(),
                desktop.inputEndDateDialog(),
                desktop.inputCurrencyDialog(),
                desktop.outputCurrencyDialog(),
                desktop.lineChartDisplay(),
                new WebService.ExchangeRateSeriesStore()
        ))
        .generateUi().setVisible(true);
    }
}