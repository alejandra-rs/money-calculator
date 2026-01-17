package software.ulpgc.moneycalculator.application.webservice;

import com.google.gson.JsonObject;
import software.ulpgc.moneycalculator.application.custom.WebService;
import software.ulpgc.moneycalculator.architecture.io.ExchangeRateStore;
import software.ulpgc.moneycalculator.architecture.model.Currency;
import software.ulpgc.moneycalculator.architecture.model.ExchangeRate;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static software.ulpgc.moneycalculator.application.custom.WebService.apiKey;

public class WebServiceExchangeRateStore implements ExchangeRateStore {

    private static final String ExchangeRateApiUrl = "https://v6.exchangerate-api.com/v6/API-KEY/".replace("API-KEY", apiKey());
    private static final String HistoricalRatesApiUrl = "https://api.frankfurter.dev/v1/";
    private static final String JsonConversionRateKey = "conversion_rate";

    @Override
    public ExchangeRate load(Currency from, Currency to, LocalDate date) {
        try {
            return exchangeRateWith(date, from, to);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private ExchangeRate exchangeRateWith(LocalDate date, Currency from, Currency to) throws IOException {
        return new ExchangeRate(
                date,
                from,
                to,
                readConversionRate(WebService.RateReader.readJsonIn(date.isEqual(LocalDate.now()) ?
                        currentUrl(from, to) :
                        historicalUrl(date, from, to)))
        );
    }

    private URL currentUrl(Currency from, Currency to) throws MalformedURLException {
        return new URL(ExchangeRateApiUrl + "pair/" + from.code() + "/" + to.code());
    }

    private URL historicalUrl(LocalDate date, Currency from, Currency to) throws MalformedURLException {
        return new URL(HistoricalRatesApiUrl +
                date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) +
                "?base=" + from.code() +
                "&symbols=" + to.code());
    }

    private double readConversionRate(JsonObject object) {
        return isCurrentRate(object) ? object.get(JsonConversionRateKey).getAsDouble()
                : WebService.RateReader.doubleIn(object.get("rates").getAsJsonObject());
    }

    private boolean isCurrentRate(JsonObject object) {
        return object.has(JsonConversionRateKey);
    }
}
