package software.ulpgc.moneycalculator.application.custom;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.Day;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import software.ulpgc.moneycalculator.architecture.viewmodel.LineChart;

import java.awt.*;
import java.time.LocalDate;

public class TimeSeriesChartBuilder {

    private final LineChart lineChart;

    private TimeSeriesChartBuilder(LineChart lineChart) {
        this.lineChart = lineChart;
    }

    public static TimeSeriesChartBuilder with(LineChart lineChart) {
        return new TimeSeriesChartBuilder(lineChart);
    }

    public Component build() {
        return new ChartPanel(decorate(chart()));
    }

    private JFreeChart decorate(JFreeChart chart) {
        return DarkGreenTheme.forTimeSeriesChart(chart.getPlot());
    }

    private JFreeChart chart() {
        return ChartFactory.createTimeSeriesChart("", "", "", dataset());
    }

    private TimeSeriesCollection dataset() {
        TimeSeriesCollection collection = new TimeSeriesCollection();
        collection.addSeries(series());
        return collection;
    }

    private TimeSeries series() {
        TimeSeries series = new TimeSeries("");
        lineChart.forEach(date -> series.add(periodOf(date), lineChart.get(date)));
        return series;
    }

    private RegularTimePeriod periodOf(LocalDate date) {
        return new Day(date.getDayOfMonth(),
                date.getMonthValue(),
                date.getYear());
    }

}