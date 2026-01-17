package software.ulpgc.moneycalculator.architecture.control;

import software.ulpgc.moneycalculator.architecture.io.ExchangeRateSeriesStore;
import software.ulpgc.moneycalculator.architecture.tasks.LineChartBuilder;
import software.ulpgc.moneycalculator.architecture.ui.ExchangeCurrencyDialog;
import software.ulpgc.moneycalculator.architecture.viewmodel.LineChart;

public class ViewHistoryCommand implements Command {

    private final ExchangeCurrencyDialog exchangeCurrencyDialog;
    private final ExchangeRateSeriesStore store;

    public ViewHistoryCommand(ExchangeCurrencyDialog exchangeCurrencyDialog, ExchangeRateSeriesStore store) {
        this.exchangeCurrencyDialog = exchangeCurrencyDialog;
        this.store = store;
    }

    @Override
    public void execute() {
        LineChart lineChart = LineChartBuilder.with(store.exchangeRatesBetween(exchangeCurrencyDialog.getFromCurrency(),
                                                                               exchangeCurrencyDialog.getToCurrency(),
                                                                               exchangeCurrencyDialog.getFromDate(),
                                                                               exchangeCurrencyDialog.getToDate())).build();
        exchangeCurrencyDialog.show(lineChart);
    }
}
