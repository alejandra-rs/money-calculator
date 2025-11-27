package software.ulpgc.moneycalculator.application.beatles;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;

import java.awt.*;

import static java.awt.Color.WHITE;

public class DarkGreenTheme {

    public static JFreeChart forTimeSeriesChart(Plot plot) {
        XYPlot xyPlot = (XYPlot) plot;
        setBackgroundTheme(xyPlot);
        setForegroundTheme(xyPlot);
        return xyPlot.getChart();
    }

    private static void setBackgroundTheme(XYPlot xyPlot) {
        xyPlot.setBackgroundPaint(WHITE);
    }

    private static void setForegroundTheme(XYPlot xyPlot) {
        setLineColor((XYLineAndShapeRenderer) xyPlot.getRenderer());
    }

    private static void setLineColor(XYLineAndShapeRenderer renderer) {
        renderer.setSeriesPaint(0, darkGreen(255));
    }

    public static Color darkGreen(int alpha) {
        return new Color(0, 80, 0, alpha);
    }

}
