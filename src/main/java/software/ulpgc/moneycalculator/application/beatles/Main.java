package software.ulpgc.moneycalculator.application.beatles;

import software.ulpgc.moneycalculator.architecture.control.ExchangeMoneyCommand;
import software.ulpgc.moneycalculator.architecture.control.HistoricalExchangeMoneyCommand;

public class Main {
    public static void main(String[] args) {
        Desktop desktop = new Desktop(new WebService.CurrencyStore().currencies(),
                                      new WebService.HistoricalCurrencyStore().currencies());
        desktop.addCommand("exchange", new ExchangeMoneyCommand(
                desktop.moneyDialog(),
                desktop.currencyDialog(),
                new WebService.ExchangeRateStore(),
                desktop.moneyDisplay()
        ));
        desktop.addCommand("historicalExchange", new HistoricalExchangeMoneyCommand(
                desktop.moneyDialog(),
                desktop.currencyDialog(),
                desktop.inputDateDialog(),
                new WebService.HistoricalExchangeRateStore(),
                desktop.moneyDisplay()
        ));
        desktop.setVisible(true);
    }
}
