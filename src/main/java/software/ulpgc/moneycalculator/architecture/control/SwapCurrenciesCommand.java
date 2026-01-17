package software.ulpgc.moneycalculator.architecture.control;

import software.ulpgc.moneycalculator.architecture.model.Currency;
import software.ulpgc.moneycalculator.architecture.ui.CurrencyPanel;

public class SwapCurrenciesCommand implements Command {

    private final CurrencyPanel currencyPanel;

    public SwapCurrenciesCommand(CurrencyPanel currencyPanel) {
        this.currencyPanel = currencyPanel;
    }

    @Override
    public void execute() {
        Currency toCurrency = currencyPanel.getToCurrency();
        currencyPanel.setToCurrency(currencyPanel.getFromCurrency());
        currencyPanel.setFromCurrency(toCurrency);
    }
}
