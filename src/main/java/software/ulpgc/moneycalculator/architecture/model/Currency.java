package software.ulpgc.moneycalculator.architecture.model;

public record Currency(String code, String country) {

    public static final Currency Null = new Currency("N/A", "N/A");

    @Override
    public String toString() {
        return code + " - " + country;
    }
}
