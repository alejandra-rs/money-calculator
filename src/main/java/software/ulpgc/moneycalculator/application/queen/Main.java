package software.ulpgc.moneycalculator.application.queen;

import software.ulpgc.moneycalculator.architecture.control.ExchangeMoneyCommand;

public class Main {
    public static void main(String[] args) {
        Desktop desktop = new Desktop(new WebService.CurrencyStore().currencies());
        desktop.addCommand("exchange", new ExchangeMoneyCommand(
                desktop.moneyDialog(),
                desktop.currencyDialog(),
                new WebService.ExchangeRateStore(),
                desktop.moneyDisplay()
        ));
        desktop.setVisible(true);
    }
}
