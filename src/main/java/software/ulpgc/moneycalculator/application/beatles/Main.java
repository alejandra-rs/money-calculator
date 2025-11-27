package software.ulpgc.moneycalculator.application.beatles;

import software.ulpgc.moneycalculator.architecture.control.ExchangeMoneyCommand;
import software.ulpgc.moneycalculator.architecture.control.HistoricalExchangeMoneyCommand;
import software.ulpgc.moneycalculator.architecture.control.ViewHistoryCommand;

public class Main {
    public static void main(String[] args) {
        Desktop desktop = new Desktop(new WebService.CurrencyStore().currencies(),
                                      new WebService.HistoricalCurrencyStore().currencies());
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
        desktop.addCommand("viewHistoryCommand", new ViewHistoryCommand(
                desktop.inputStartDateDialog(),
                desktop.inputEndDateDialog(),
                desktop.inputCurrencyDialog(),
                desktop.outputCurrencyDialog(),
                desktop.lineChartDisplay(),
                new WebService.ExchangeRateSeriesStore()
        ));
        desktop.setVisible(true);
    }
}
