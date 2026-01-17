package software.ulpgc.moneycalculator.application.webservice;

import software.ulpgc.moneycalculator.architecture.model.Currency;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ExchangeApi {

    private static final String apiKey = loadApiKey();
    private static final String currentBaseUrl = "https://v6.exchangerate-api.com/v6/API-KEY/".replace("API-KEY", apiKey);
    private static final String historicalBaseUrl = "https://api.frankfurter.dev/v1/";

    private static String loadApiKey() {
        try (InputStream is = ExchangeApi.class.getResourceAsStream("/api-key.txt")) {
            return new String(is.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    public static URL currentCurrenciesUrl() throws MalformedURLException {
        return new URL(currentBaseUrl + "codes");
    }

    public static URL historicalCurrenciesUrl() throws MalformedURLException {
        return new URL(historicalBaseUrl + "currencies");
    }

    public static URL currentRateUrl(Currency from, Currency to) throws MalformedURLException {
        return new URL(currentBaseUrl + "pair/" + from.code() + "/" + to.code());
    }

    public static URL historicalRateUrl(LocalDate date, Currency from, Currency to) throws MalformedURLException {
        return new URL(historicalBaseUrl +
                date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) +
                "?base=" + from.code() + "&symbols=" + to.code());
    }

    public static URL seriesUrl(Currency from, Currency to, LocalDate start, LocalDate end) throws MalformedURLException {
        return new URL(historicalBaseUrl + start + ".." + end +
                "?base=" + from.code() + "&symbols=" + to.code());
    }
}
