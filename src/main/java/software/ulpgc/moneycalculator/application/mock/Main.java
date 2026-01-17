package software.ulpgc.moneycalculator.application.mock;

import software.ulpgc.moneycalculator.architecture.control.Command;
import software.ulpgc.moneycalculator.architecture.control.ExchangeMoneyCommand;
import software.ulpgc.moneycalculator.architecture.model.Currency;
import software.ulpgc.moneycalculator.architecture.model.Money;
import software.ulpgc.moneycalculator.architecture.ui.ExchangeMoneyDialog;

import java.time.LocalDate;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        List<Currency> currencies = new MockCurrencyStore().currencies().toList();
        Command command = new ExchangeMoneyCommand(
                exchangeMoneyDialog(currencies),
                new MockExchangeRateStore()
        );
        command.execute();
    }

    private static ExchangeMoneyDialog exchangeMoneyDialog(List<Currency> currencies) {
        return new ExchangeMoneyDialog() {
            @Override
            public Money getMoney() {
                return new Money(100, currencies.getFirst());
            }

            @Override
            public LocalDate getDate() {
                return LocalDate.now();
            }

            @Override
            public void show(Money money) {
                System.out.println(money);
            }

            @Override
            public Currency getFromCurrency() {
                return currencies.getFirst();
            }

            @Override
            public Currency getToCurrency() {
                return currencies.get(1);
            }

            @Override
            public void setFromCurrency(Currency currency) {}

            @Override
            public void setToCurrency(Currency currency) {}
        };
    }
}
