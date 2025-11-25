package software.ulpgc.moneycalculator.architecture.tasks;

import software.ulpgc.moneycalculator.architecture.model.ExchangeRate;
import software.ulpgc.moneycalculator.architecture.viewmodel.LineChart;

import java.util.stream.Stream;

public class LineChartBuilder {

    private final Stream<ExchangeRate> rates;

    private LineChartBuilder(Stream<ExchangeRate> rates) {
        this.rates = rates;
    }

    public static LineChartBuilder with(Stream<ExchangeRate> rates) {
        return new LineChartBuilder(rates);
    }

    public LineChart build() {
        LineChart lineChart = new LineChart();
        rates.forEach(r -> lineChart.add(r.date(), r.rate()));
        return lineChart;
    }
}
