package software.ulpgc.moneycalculator.architecture.control;

import software.ulpgc.moneycalculator.architecture.io.ExchangeRateStore;
import software.ulpgc.moneycalculator.architecture.model.Currency;
import software.ulpgc.moneycalculator.architecture.model.ExchangeRate;
import software.ulpgc.moneycalculator.architecture.model.Money;
import software.ulpgc.moneycalculator.architecture.ui.CurrencyDialog;
import software.ulpgc.moneycalculator.architecture.ui.DateDialog;
import software.ulpgc.moneycalculator.architecture.ui.MoneyDialog;
import software.ulpgc.moneycalculator.architecture.ui.MoneyDisplay;

public class HistoricalExchangeMoneyCommand implements Command {

    private final MoneyDialog moneyDialog;
    private final CurrencyDialog currencyDialog;
    private final DateDialog dateDialog;
    private final ExchangeRateStore store;
    private final MoneyDisplay moneyDisplay;

    public HistoricalExchangeMoneyCommand(MoneyDialog moneyDialog, CurrencyDialog currencyDialog, DateDialog dateDialog, ExchangeRateStore store, MoneyDisplay moneyDisplay) {
        this.moneyDialog = moneyDialog;
        this.currencyDialog = currencyDialog;
        this.dateDialog = dateDialog;
        this.store = store;
        this.moneyDisplay = moneyDisplay;
    }

    @Override
    public void execute() {
        Money money = moneyDialog.get();
        Currency currency = currencyDialog.get();

        ExchangeRate exchangeRate = store.load(money.currency(), currency, dateDialog.get());

        Money result = new Money(money.amount() * exchangeRate.rate(), currency);
        moneyDisplay.show(result);
    }

}
