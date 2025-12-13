package software.ulpgc.moneycalculator.application.beatles;

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;
import software.ulpgc.moneycalculator.architecture.control.Command;
import software.ulpgc.moneycalculator.architecture.model.Currency;
import software.ulpgc.moneycalculator.architecture.model.Money;
import software.ulpgc.moneycalculator.architecture.ui.*;
import software.ulpgc.moneycalculator.architecture.viewmodel.LineChart;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.net.URL;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.awt.*;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.awt.BorderLayout.*;
import static java.awt.GridBagConstraints.RELATIVE;
import static java.awt.Image.SCALE_SMOOTH;
import static software.ulpgc.moneycalculator.application.beatles.Desktop.ButtonFactory.createButton;
import static software.ulpgc.moneycalculator.application.beatles.Desktop.ComponentFactory.*;
import static software.ulpgc.moneycalculator.application.beatles.Desktop.Mode.*;

public class Desktop extends JFrame {

    public enum Mode {CURRENT, HISTORY, GRAPHICS}

    private static final GridBagConstraints panelConstraints = gridBagConstraints();
    private static GridBagConstraints gridBagConstraints() {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(3, 3, 3, 3);
        constraints.anchor = GridBagConstraints.LINE_END;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1.0;
        return constraints;
    }

    private final UiElementFactory uiElementFactory = new UiElementFactory();
    private final Map<String, Command> commands;
    private final List<Currency> currencies, historicalCurrencies;
    private JTextField inputAmount, outputAmount;
    private JComboBox<Currency> inputCurrency, outputCurrency;
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
        loadMode(Mode.CURRENT);
        return this;

    }

    private Desktop setWindowProperties() {
        this.setTitle("Money Calculator");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(800,500);
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        return this;
    }

    private void clear() {
        this.getContentPane().removeAll();
        this.getContentPane().revalidate();
        this.getContentPane().repaint();
    }

    private void loadMode(Mode mode) {
        clear();
        this.getContentPane().add(modeButtons(), NORTH);
        this.getContentPane().add(mode == Mode.CURRENT ? currentModeCenterPanel() :
                                  mode == Mode.HISTORY ? historyModeCenterPanel() :
                                                         graphicsModeCenterPanel());
    }

    private JPanel modeButtons() {
        JPanel panel = new JPanel(new FlowLayout());
        panel.setBackground(DarkGreenTheme.darkGreen(128));
        addModeButtonsTo(panel);
        return panel;
    }

    private void addModeButtonsTo(JPanel panel) {
        panel.add(decorateAsHeader(createButton("Current Currencies Mode", e -> loadMode(Mode.CURRENT))));
        panel.add(decorateAsHeader(createButton("History Mode", e -> loadMode(Mode.HISTORY))));
        panel.add(decorateAsHeader(createButton("Graphics Mode", e -> loadMode(GRAPHICS))));
    }

    private JButton decorateAsHeader(JButton button) {
        return DarkGreenTheme.forHeaderButton(button);
    }

    private JButton swapCurrenciesButton() {
        return createButton(Main.class.getResource("/swap.png"), e -> swapCurrencies());
    }

    private JPanel buildPanelWith(JPanel panel, Component... components) {
        Arrays.stream(components).forEach(panel::add);
        return panel;
    }

    private JPanel currentModeCenterPanel() {
        JPanel centerPanel = DarkGreenTheme.setBackgroundFor(new JPanel(new BorderLayout()));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        centerPanel.add(exchangePanelWith(currencies), CENTER);
        centerPanel.add(buildPanelWith(nonOpaquePanel(new FlowLayout()),
                DarkGreenTheme.forHeaderButton(createButton("Exchange", e -> exchangeMoney()))), SOUTH);

        return panelWithBorderFor(CURRENT, centerPanel);
    }

    private JPanel historyModeCenterPanel() {
        JPanel centerPanel = DarkGreenTheme.setBackgroundFor(new JPanel(new BorderLayout()));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        centerPanel.add(buildPanelWith(nonOpaquePanel(new FlowLayout()), inputDate = dateChooser()), NORTH);
        centerPanel.add(exchangePanelWith(historicalCurrencies), CENTER);
        centerPanel.add(buildPanelWith(nonOpaquePanel(new FlowLayout()),
                DarkGreenTheme.forHeaderButton(createButton("Exchange", e -> exchangeHistoricalMoney()))), SOUTH);

        return panelWithBorderFor(HISTORY, centerPanel);
    }

    private JPanel graphicsModeCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel controlsContainer = new JPanel(new BorderLayout());
        controlsContainer.setBackground(DarkGreenTheme.darkGreen(50));

        controlsContainer.add(panelWithBorderFor(GRAPHICS, graphicsPanel()), CENTER);
        controlsContainer.add(panelWithBorderFor(GRAPHICS, buildPanelWith(nonOpaquePanel(new GridBagLayout()),
                DarkGreenTheme.forHeaderButton(createButton("Generate graphics", e -> generateGraphics())))), EAST);

        panel.add(controlsContainer, NORTH);
        panel.add(outputChart, CENTER);

        return panel;
    }

    private JPanel nonOpaquePanel(LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setOpaque(false);
        return panel;
    }

    private JPanel panelWithBorderFor(Mode mode, JPanel centerPanel) {
        JPanel panel = nonOpaquePanel(new BorderLayout());

        if (mode == GRAPHICS) panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        else panel.setBorder(BorderFactory.createEmptyBorder(100, 30, 100, 30));

        panel.add(centerPanel, CENTER);
        return panel;
    }

    private JPanel exchangePanelWith(List<Currency> currencies) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        addToRow(panel, 0, inputAmount = amountInput(), nonOpaquePanel(new FlowLayout()), outputAmount = amountOutput());
        addToRow(panel, 1, inputCurrency = currencySelectorWith(currencies), swapCurrenciesButton(), outputCurrency = currencySelectorWith(currencies));
        return panel;
    }

    private JPanel graphicsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        addToRow(panel, 0, inputStartDate = dateChooser(), arrowLabel(), inputEndDate = dateChooser());
        addToRow(panel, 1, inputCurrency = graphicsCurrencySelectorWith(historicalCurrencies),
                swapCurrenciesButton(),
                outputCurrency = graphicsCurrencySelectorWith(historicalCurrencies));
        return panel;
    }

    private JPanel arrowLabel() {
        JLabel label = new JLabel("→");
        label.setFont(new Font("Arial", Font.PLAIN, 40));
        return buildPanelWith(nonOpaquePanel(new FlowLayout(FlowLayout.CENTER)), label);
    }

    private void addToRow(JPanel panel, int nrow, Component... components) {
        IntStream.range(0, components.length)
                .forEach(i -> addComponent(panel, components[i], nrow));
    }

    private void addComponent(JPanel panel, Component component, int nrow) {
        panelConstraints.gridx = RELATIVE;
        panelConstraints.gridy = nrow;
        panel.add(component, panelConstraints);
    }

    private void exchangeMoney() {
        if (uiElementFactory.inputAmount() == 0) showErrorPanel("Please introduce a valid amount of money.");
        else commands.get("exchange").execute();
    }

    private void exchangeHistoricalMoney() {
        if (uiElementFactory.inputAmount() == 0) showErrorPanel("Please introduce a valid amount of money.");
        else commands.get("historicalExchange").execute();
    }

    private void generateGraphics() {
        if (inputStartDate.getDate().isAfter(inputEndDate.getDate())) showErrorPanel("The start date must be chronologically before the end date.");
        else commands.get("generateGraphics").execute();
    }

    private void swapCurrencies() {
        int inputIndex = inputCurrency.getSelectedIndex();
        inputCurrency.setSelectedIndex(outputCurrency.getSelectedIndex());
        outputCurrency.setSelectedIndex(inputIndex);
        outputAmount.setText("");
    }

    private void showErrorPanel(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    public UiElementFactory uiElementFactory() {
        return uiElementFactory;
    }

    public class UiElementFactory {

        public UiElementFactory() {}

        public MoneyDialog moneyDialog() {
            return () -> new Money(inputAmount(), inputCurrency());
        }

        public CurrencyDialog inputCurrencyDialog() {
            return this::inputCurrency;
        }

        private Currency inputCurrency() {
            return (Currency) inputCurrency.getSelectedItem();
        }

        public CurrencyDialog outputCurrencyDialog() {
            return () -> (Currency) outputCurrency.getSelectedItem();
        }

        public MoneyDisplay moneyDisplay() {
            return money -> outputAmount.setText(String.format("%10f", money.amount()));
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
            try {
                return toDouble(inputAmount.getText());
            } catch (NumberFormatException e) {
                return 0;
            }
        }

        private double toDouble(String text) throws NumberFormatException {
            return Double.parseDouble(text);
        }
    }

    public static class ComponentFactory {

        public static JTextField amountInput() {
            return DarkGreenTheme.forTextField(new JTextField(10));
        }

        public static JTextField amountOutput() {
            JTextField textField = DarkGreenTheme.forTextField(new JTextField(10));
            textField.setEditable(false);
            return textField;
        }

        public static DatePicker dateChooser() {
            DatePickerSettings settings = dateSettings();
            DatePicker datePicker = DarkGreenTheme.forDatePicker(new DatePicker(settings));
            datePicker.setDate(LocalDate.now().minusDays(1));
            settings.setDateRangeLimits(LocalDate.of(1999, 1, 1), LocalDate.now().minusDays(1));
            return datePicker;
        }

        public static DatePickerSettings dateSettings() {
            DatePickerSettings dateSettings = new DatePickerSettings();
            dateSettings.setAllowEmptyDates(false);
            return dateSettings;
        }

        public static JComboBox<Currency> currencySelectorWith(List<Currency> currencies) {
            return DarkGreenTheme.forComboBox(new JComboBox<>(toArray(currencies)));
        }

        public static JComboBox<Currency> graphicsCurrencySelectorWith(List<Currency> currencies) {
            return DarkGreenTheme.forGraphicsComboBox(new JComboBox<>(toArray(currencies)));
        }

        private static Currency[] toArray(List<Currency> currencies) {
            return currencies.toArray(new Currency[0]);
        }
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
