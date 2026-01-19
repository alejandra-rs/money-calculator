package software.ulpgc.moneycalculator.architecture.ui;

import software.ulpgc.moneycalculator.architecture.viewmodel.LineChart;

import java.time.LocalDate;

public interface ExchangeCurrencyDialog extends CurrencyPanel {
    LocalDate getFromDate();
    LocalDate getToDate();

    void show(LineChart linechart);
}
