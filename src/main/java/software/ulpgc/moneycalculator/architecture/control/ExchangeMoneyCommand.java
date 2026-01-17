package software.ulpgc.moneycalculator.architecture.control;

import software.ulpgc.moneycalculator.architecture.io.ExchangeRateStore;
import software.ulpgc.moneycalculator.architecture.model.Currency;
import software.ulpgc.moneycalculator.architecture.model.ExchangeRate;
import software.ulpgc.moneycalculator.architecture.model.Money;
import software.ulpgc.moneycalculator.architecture.ui.ExchangeMoneyDialog;

import java.time.LocalDate;

public class ExchangeMoneyCommand implements Command {
    private final ExchangeMoneyDialog exchangeMoneyDialog;
    private final ExchangeRateStore store;

    public ExchangeMoneyCommand(ExchangeMoneyDialog exchangeMoneyDialog, ExchangeRateStore store) {
        this.exchangeMoneyDialog = exchangeMoneyDialog;
        this.store = store;
    }

    @Override
    public void execute() {
        Money money = exchangeMoneyDialog.getMoney();
        Currency currency = exchangeMoneyDialog.getToCurrency();
        LocalDate date = exchangeMoneyDialog.getDate();

        ExchangeRate exchangeRate = store.load(money.currency(), currency, date);

        Money result = new Money(money.amount() * exchangeRate.rate(), currency);
        exchangeMoneyDialog.show(result);
    }
}
