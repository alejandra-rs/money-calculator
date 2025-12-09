package software.ulpgc.moneycalculator.application.beatles;

import com.github.lgooddatepicker.components.DatePicker;
import software.ulpgc.moneycalculator.architecture.control.Command;
import software.ulpgc.moneycalculator.architecture.model.Currency;
import software.ulpgc.moneycalculator.architecture.model.Money;
import software.ulpgc.moneycalculator.architecture.ui.*;
import software.ulpgc.moneycalculator.architecture.viewmodel.LineChart;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.awt.BorderLayout.NORTH;
import static java.awt.FlowLayout.CENTER;
import static java.awt.Image.SCALE_SMOOTH;
import static software.ulpgc.moneycalculator.application.beatles.Desktop.ButtonFactory.createButton;

public class Desktop extends JFrame {
    private final Map<String, Command> commands;
    private final List<Currency> currencies, historicalCurrencies;
    private JTextField inputAmount;
    private JComboBox<Currency> inputCurrency;
    private JTextField outputAmount;
    private JComboBox<Currency> outputCurrency;
    private DatePicker inputDate, inputStartDate, inputEndDate;
    private final JPanel outputChart = new JPanel(new BorderLayout());

    private Desktop(Stream<Currency> currencies, Stream<Currency> historicalCurrencies) {
        this.commands = new HashMap<>();
        this.currencies = currencies.toList();
        this.historicalCurrencies = historicalCurrencies.toList();
    }

    public static Desktop with(Stream<Currency> currencies, Stream<Currency> historicalCurrencies) {
        return new Desktop(currencies, historicalCurrencies);
    }

    public Desktop addCommand(String name, Command command) {
        this.commands.put(name, command);
        return this;
    }

    public Desktop generateUi() throws Exception {
        UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        this.setWindowProperties().setLayout(new BorderLayout());
        currentCurrenciesMode();
        return this;

    }

    private Desktop setWindowProperties() {
        this.setTitle("Money Calculator");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setSize(800,500);
        this.setResizable(false);
        return this;
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
        panel.add(createButton("Exchange", e -> exchangeMoney()));
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
        panel.add(createButton("Exchange", e -> exchangeHistoricalMoney()));
        this.getContentPane().add(panel);
    }

    private void graphicsMode() {
        clear();
        this.getContentPane().add(modeButtons(), NORTH);
        JPanel panel = new JPanel(new BorderLayout());
        JPanel controls = new JPanel();
        controls.add(inputStartDate = dateChooser());
        controls.add(inputEndDate = dateChooser());
        controls.add(inputCurrency = historicalCurrencySelector());
        controls.add(swapCurrenciesButton());
        controls.add(outputCurrency = historicalCurrencySelector());
        controls.add(createButton("Generate Graphics", e -> generateGraphics()));
        panel.add(controls, NORTH);
        panel.add(outputChart);
        this.getContentPane().add(panel);
    }

    private void exchangeMoney() {
        if (inputAmount() == 0) showErrorPanel("Please introduce a valid amount of money.");
        else commands.get("exchange").execute();
    }

    private void exchangeHistoricalMoney() {
        if (inputAmount() == 0) showErrorPanel("Please introduce a valid amount of money.");
        else commands.get("historicalExchange").execute();
    }

    private void generateGraphics() {
        if (inputStartDate.getDate().isAfter(inputEndDate.getDate())) showErrorPanel("The start date must be chronologically before the end date.");
        else commands.get("generateGraphics").execute();
    }

    private void showErrorPanel(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    private JButton swapCurrenciesButton() {
        return createButton(Main.class.getResource("/swap.png"), e -> swapCurrencies());
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

    public static class ButtonFactory {

        public static JButton createButton(String text, ActionListener listener) {
            JButton jButton = new JButton(text);
            jButton.addActionListener(listener);
            return jButton;
        }

        public static JButton createButton(URL imageResource, ActionListener listener) {
            JButton jButton = buttonWith(imageResource);
            jButton.addActionListener(listener);
            return jButton;
        }

        private static JButton buttonWith(URL resource) {
            return buttonWith(new ImageIcon(resource));
        }

        private static JButton buttonWith(ImageIcon imageIcon) {
            return buttonWith(imageIcon.getImage().getScaledInstance(20, 20, SCALE_SMOOTH));
        }

        private static JButton buttonWith(Image image) {
            return new JButton(new ImageIcon(image));
        }
    }
}
