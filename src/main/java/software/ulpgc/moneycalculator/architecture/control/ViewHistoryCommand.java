package software.ulpgc.moneycalculator.architecture.control;

import software.ulpgc.moneycalculator.architecture.io.ExchangeRateSeriesStore;
import software.ulpgc.moneycalculator.architecture.model.Currency;
import software.ulpgc.moneycalculator.architecture.tasks.LineChartBuilder;
import software.ulpgc.moneycalculator.architecture.ui.CurrencyDialog;
import software.ulpgc.moneycalculator.architecture.ui.DateDialog;
import software.ulpgc.moneycalculator.architecture.ui.LineChartDisplay;
import software.ulpgc.moneycalculator.architecture.viewmodel.LineChart;

public class ViewHistoryCommand implements Command {

    private final DateDialog startDateDialog;
    private final DateDialog endDateDialog;
    private final CurrencyDialog inputCurrencyDialog;
    private final CurrencyDialog outputCurrencyDialog;
    private final LineChartDisplay lineChartDisplay;
    private final ExchangeRateSeriesStore store;

    public ViewHistoryCommand(DateDialog startDateDialog, DateDialog endDateDialog, CurrencyDialog inputCurrencyDialog, CurrencyDialog outputCurrencyDialog, LineChartDisplay lineChartDisplay, ExchangeRateSeriesStore store) {
        this.startDateDialog = startDateDialog;
        this.endDateDialog = endDateDialog;
        this.inputCurrencyDialog = inputCurrencyDialog;
        this.outputCurrencyDialog = outputCurrencyDialog;
        this.lineChartDisplay = lineChartDisplay;
        this.store = store;
    }

    @Override
    public void execute() {

        Currency from = inputCurrencyDialog.get();
        Currency to = outputCurrencyDialog.get();

        LineChart lineChart = LineChartBuilder.with(store.exchangeRatesBetween(from, to, startDateDialog.get(), endDateDialog.get()))
                .build();

        lineChartDisplay.show(lineChart);
    }
}
