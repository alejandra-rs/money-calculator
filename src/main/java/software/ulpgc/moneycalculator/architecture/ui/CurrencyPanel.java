package software.ulpgc.moneycalculator.architecture.ui;

import software.ulpgc.moneycalculator.architecture.model.Currency;

public interface CurrencyPanel {
    Currency getFromCurrency();
    Currency getToCurrency();

    void setFromCurrency(Currency currency);
    void setToCurrency(Currency currency);
}
