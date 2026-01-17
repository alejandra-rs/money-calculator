package software.ulpgc.moneycalculator.application.queen;

import software.ulpgc.moneycalculator.architecture.control.Command;
import software.ulpgc.moneycalculator.architecture.model.Currency;
import software.ulpgc.moneycalculator.architecture.model.Money;
import software.ulpgc.moneycalculator.architecture.ui.ExchangeMoneyDialog;

import javax.swing.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.awt.*;
import java.util.Map;
import java.util.stream.Stream;

import static java.awt.FlowLayout.CENTER;

public class Desktop extends JFrame {
    private final Map<String, Command> commands;
    private final List<Currency> currencies;
    private JTextField inputAmount;
    private JComboBox<Currency> inputCurrency;
    private JTextField outputAmount;
    private JComboBox<Currency> outputCurrency;

    public Desktop(Stream<Currency> currencies) throws HeadlessException {
        this.commands = new HashMap<>();
        this.currencies = currencies.toList();
        this.setTitle("Money Calculator");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(800,600);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        this.setLayout(new BorderLayout());
        this.getContentPane().add(panel());
    }

    private JPanel panel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(CENTER));
        panel.add(inputAmount = amountInput());
        panel.add(inputCurrency = currencySelector());
        panel.add(outputAmount = amountOutput());
        panel.add(outputCurrency = currencySelector());
        panel.add(calculateButton());
        return panel;
    }

    private Component calculateButton() {
        JButton button = new JButton("Exchange");
        button.addActionListener(e -> commands.get("exchange").execute());
        return button;
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

    public ExchangeMoneyDialog exchangeMoneyDialog() {
        return new ExchangeMoneyDialog() {
            @Override
            public Money getMoney() {
                return new Money(inputAmount(), inputCurrency());
            }

            @Override
            public LocalDate getDate() {
                return LocalDate.now();
            }

            @Override
            public void show(Money money) {
                outputAmount.setText(money.amount() + "");
            }

            @Override
            public Currency getFromCurrency() {
                return inputCurrency();
            }

            @Override
            public Currency getToCurrency() {
                return outputCurrency();
            }

            @Override
            public void setFromCurrency(Currency currency) {}

            @Override
            public void setToCurrency(Currency currency) {}
        };
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
