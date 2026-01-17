package software.ulpgc.moneycalculator.architecture.ui;

import software.ulpgc.moneycalculator.architecture.model.Money;

import java.time.LocalDate;

public interface ExchangeMoneyDialog extends CurrencyPanel {
    Money getMoney();
    LocalDate getDate();

    void show(Money money);
}
