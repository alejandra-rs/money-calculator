package software.ulpgc.moneycalculator.application.custom;

import com.github.lgooddatepicker.components.DatePicker;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import software.ulpgc.moneycalculator.architecture.model.Currency;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

import static java.awt.Color.WHITE;

public class DarkGreenTheme {

    public static final Font headerFont = new Font("Arial", Font.BOLD, 26);
    public static final Font normalFont = new Font("Arial", Font.PLAIN, 18);

    public static JButton forHeaderButton(JButton button) {
        button.setBorder(new CompoundBorder(border(), padding()));
        button.setForeground(darkGreen(255));
        button.setFont(headerFont);
        return button;
    }

    public static JFreeChart forTimeSeriesChart(Plot plot) {
        XYPlot xyPlot = (XYPlot) plot;
        setBackgroundTheme(xyPlot);
        setForegroundTheme(xyPlot);
        return xyPlot.getChart();
    }

    public static JTextField forTextField(JTextField textField) {
        textField.setFont(normalFont);
        return textField;
    }

    public static JComboBox<Currency> forComboBox(JComboBox<Currency> comboBox) {
        comboBox.setFont(normalFont);
        comboBox.setPreferredSize(new Dimension(300, 30));
        return comboBox;
    }

    public static JComboBox<Currency> forGraphicsComboBox(JComboBox<Currency> comboBox) {
        comboBox.setFont(normalFont);
        comboBox.setMaximumSize(new Dimension(50, 30));
        comboBox.setMinimumSize(new Dimension(50, 30));
        comboBox.setPreferredSize(new Dimension(50, 30));
        return comboBox;
    }

    public static DatePicker forDatePicker(DatePicker datePicker) {
        datePicker.getComponentDateTextField().setPreferredSize(new Dimension(205, 30));
        datePicker.addDateChangeListener(e -> SwingUtilities.invokeLater(() -> applyFontTo(datePicker)));
        return datePicker;
    }

    public static JPanel setBackgroundFor(JPanel panel) {
        panel.setBackground(DarkGreenTheme.darkGreen(150));
        return panel;
    }


    private static LineBorder border() {
        return new LineBorder(darkGreen(255), 2);
    }

    private static EmptyBorder padding() {
        return new EmptyBorder(2, 4, 2, 4);
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

    private static void applyFontTo(DatePicker datePicker) {
        datePicker.getComponentDateTextField().setFont(normalFont);
    }
}
