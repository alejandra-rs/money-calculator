package software.ulpgc.moneycalculator.application.beatles;

import software.ulpgc.moneycalculator.architecture.control.Command;
import software.ulpgc.moneycalculator.architecture.model.Currency;
import software.ulpgc.moneycalculator.architecture.model.Money;
import software.ulpgc.moneycalculator.architecture.ui.CurrencyDialog;
import software.ulpgc.moneycalculator.architecture.ui.MoneyDialog;
import software.ulpgc.moneycalculator.architecture.ui.MoneyDisplay;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.awt.BorderLayout.NORTH;
import static java.awt.FlowLayout.CENTER;
import static java.awt.Image.SCALE_SMOOTH;

public class Desktop extends JFrame {
    private final Map<String, Command> commands;
    private final List<Currency> currencies;
    private JTextField inputAmount;
    private JComboBox<Currency> inputCurrency;
    private JTextField outputAmount;
    private JComboBox<Currency> outputCurrency;

    public Desktop(List<Currency> currencies) throws HeadlessException {
        this.commands = new HashMap<>();
        this.currencies = currencies;
        this.setTitle("Money Calculator");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(800,600);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        this.setLayout(new BorderLayout());
        this.getContentPane().add(modeButtons(), NORTH);
        this.getContentPane().add(currentCurrenciesMode());
    }

    private JPanel currentCurrenciesMode() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(CENTER));
        panel.add(inputAmount = amountInput());
        panel.add(inputCurrency = currencySelector());
        panel.add(swapCurrenciesButton());
        panel.add(outputAmount = amountOutput());
        panel.add(outputCurrency = currencySelector());
        panel.add(calculateButton());
        return panel;
    }

    private JPanel modeButtons() {
        JPanel panel = new JPanel(new FlowLayout());
        panel.add(currentCurrenciesModeButton());
        panel.add(historyModeButton());
        return panel;
    }

    private JButton currentCurrenciesModeButton() {
        JButton button = new JButton("Current Currencies Mode");
        button.addActionListener(e -> currentCurrenciesMode());
        return button;
    }

    private JButton historyModeButton() {
        return new JButton("History Mode");
    }

    private Component calculateButton() {
        JButton button = new JButton("Exchange");
        button.addActionListener(e -> commands.get("exchange").execute());
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

    private Currency[] toArray(List<Currency> currencies) {
        return currencies.toArray(new Currency[0]);
    }


    public void addCommand(String name, Command command) {
        this.commands.put(name, command);
    }

    public MoneyDialog moneyDialog() {
        return () -> new Money(inputAmount(), inputCurrency());
    }

    public CurrencyDialog currencyDialog() {
        return this::outputCurrency;
    }

    public MoneyDisplay moneyDisplay() {
        return money -> outputAmount.setText(money.amount() + "");
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
