package software.ulpgc.moneycalculator.application.beatles;

import com.github.lgooddatepicker.components.DatePicker;
import software.ulpgc.moneycalculator.architecture.control.Command;
import software.ulpgc.moneycalculator.architecture.model.Currency;
import software.ulpgc.moneycalculator.architecture.model.Money;
import software.ulpgc.moneycalculator.architecture.ui.*;
import software.ulpgc.moneycalculator.architecture.viewmodel.LineChart;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.awt.BorderLayout.NORTH;
import static java.awt.FlowLayout.CENTER;
import static java.awt.Image.SCALE_SMOOTH;

public class Desktop extends JFrame {
    private final Map<String, Command> commands;
    private final List<Currency> currencies, historicalCurrencies;
    private JTextField inputAmount;
    private JComboBox<Currency> inputCurrency;
    private JTextField outputAmount;
    private JComboBox<Currency> outputCurrency;
    private DatePicker inputDate, inputStartDate, inputEndDate;
    private final JPanel outputChart = new JPanel(new BorderLayout());

    public Desktop(Stream<Currency> currencies, Stream<Currency> historicalCurrencies) throws HeadlessException {
        this.commands = new HashMap<>();
        this.currencies = currencies.toList();
        this.historicalCurrencies = historicalCurrencies.toList();
        this.setTitle("Money Calculator");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(800,600);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        this.setLayout(new BorderLayout());
        currentCurrenciesMode();
    }

    private void clear() {
        this.getContentPane().removeAll();
        this.getContentPane().revalidate();
        this.getContentPane().repaint();
    }

    private JPanel modeButtons() {
        JPanel panel = new JPanel(new FlowLayout());
        panel.add(currentCurrenciesModeButton());
        panel.add(historyModeButton());
        panel.add(graphicsModeButton());
        return panel;
    }

    private JButton currentCurrenciesModeButton() {
        JButton button = new JButton("Current Currencies Mode");
        button.addActionListener(e -> currentCurrenciesMode());
        return button;
    }

    private JButton historyModeButton() {
        JButton button = new JButton("History Mode");
        button.addActionListener(e -> historyMode());
        return button;
    }

    private JButton graphicsModeButton() {
        JButton button = new JButton("Graphics Mode");
        button.addActionListener(e -> graphicsMode());
        return button;
    }

    private void currentCurrenciesMode() {
        clear();
        this.getContentPane().add(modeButtons(), NORTH);
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(CENTER));
        panel.add(inputAmount = amountInput());
        panel.add(inputCurrency = currencySelector());
        panel.add(swapCurrenciesButton());
        panel.add(outputAmount = amountOutput());
        panel.add(outputCurrency = currencySelector());
        panel.add(calculateButton());
        this.getContentPane().add(panel);
    }

    private void historyMode() {
        clear();
        this.getContentPane().add(modeButtons(), NORTH);
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(CENTER));
        panel.add(inputDate = dateChooser());
        panel.add(inputAmount = amountInput());
        panel.add(inputCurrency = historicalCurrencySelector());
        panel.add(swapCurrenciesButton());
        panel.add(outputAmount = amountOutput());
        panel.add(outputCurrency = historicalCurrencySelector());
        panel.add(historicalCalculateButton());
        this.getContentPane().add(panel);
    }

    private void graphicsMode() {
        clear();
        this.getContentPane().add(modeButtons(), NORTH);
        JPanel controls = new JPanel();
        controls.add(inputStartDate = dateChooser());
        controls.add(inputEndDate = dateChooser());
        controls.add(inputCurrency = historicalCurrencySelector());
        controls.add(swapCurrenciesButton());
        controls.add(outputCurrency = historicalCurrencySelector());
        controls.add(calculateButton());
        outputChart.add(controls, NORTH);
        this.getContentPane().add(outputChart);
    }

    private JButton calculateButton() {
        JButton button = new JButton("Exchange");
        button.addActionListener(e -> commands.get("exchange").execute());
        return button;
    }

    private JButton historicalCalculateButton() {
        JButton button = new JButton("Exchange");
        button.addActionListener(e -> commands.get("historicalExchange").execute());
        return button;
    }

    private JButton swapCurrenciesButton() {
        JButton jButton = buttonWith(Main.class.getResource("/swap.png"));
        jButton.addActionListener(e -> swapCurrencies());
        return jButton;
    }

    private JButton buttonWith(URL resource) {
        return buttonWith(new ImageIcon(resource));
    }

    private static JButton buttonWith(ImageIcon imageIcon) {
        return buttonWith(imageIcon.getImage().getScaledInstance(20, 20, SCALE_SMOOTH));
    }

    private static JButton buttonWith(Image image) {
        return new JButton(new ImageIcon(image));
    }

    private void swapCurrencies() {
        int inputIndex = inputCurrency.getSelectedIndex();
        inputCurrency.setSelectedIndex(outputCurrency.getSelectedIndex());
        outputCurrency.setSelectedIndex(inputIndex);
        outputAmount.setText("");
    }

    private JTextField amountInput() {
        return new JTextField(10);
    }

    private JTextField amountOutput() {
        JTextField textField = new JTextField(10);
        textField.setEditable(false);
        return textField;
    }

    private JComboBox<Currency> currencySelector() {
        return new JComboBox<>(toArray(currencies));
    }

    private JComboBox<Currency> historicalCurrencySelector() {
        return new JComboBox<>(toArray(historicalCurrencies));
    }

    private Currency[] toArray(List<Currency> currencies) {
        return currencies.toArray(new Currency[0]);
    }

    public static DatePicker dateChooser() {
        return new DatePicker();
    }

    public void addCommand(String name, Command command) {
        this.commands.put(name, command);
    }

    public MoneyDialog moneyDialog() {
        return () -> new Money(inputAmount(), inputCurrency());
    }

    public CurrencyDialog inputCurrencyDialog() {
        return this::inputCurrency;
    }

    public CurrencyDialog outputCurrencyDialog() {
        return this::outputCurrency;
    }

    public MoneyDisplay moneyDisplay() {
        return money -> outputAmount.setText(money.amount() + "");
    }

    public DateDialog inputDateDialog() {
        return () -> inputDate.getDate();
    }

    public DateDialog inputStartDateDialog() {
        return () -> inputStartDate.getDate();
    }

    public DateDialog inputEndDateDialog() {
        return () -> inputEndDate.getDate();
    }

    public LineChartDisplay lineChartDisplay() {
        return this::display;
    }

    private void display(LineChart lineChart) {
        outputChart.removeAll();
        outputChart.add(TimeSeriesChartBuilder.with(lineChart).build(), BorderLayout.CENTER);
        outputChart.revalidate();
    }

    private double inputAmount() {
        return toDouble(inputAmount.getText());
    }

    private double toDouble(String text) {
        return Double.parseDouble(text);
    }

    private Currency inputCurrency() {
        return (Currency) inputCurrency.getSelectedItem();
    }

    private Currency outputCurrency() {
        return (Currency) outputCurrency.getSelectedItem();
    }
}
