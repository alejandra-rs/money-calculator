package software.ulpgc.moneycalculator.application.webservice;

import com.google.gson.JsonObject;
import software.ulpgc.moneycalculator.architecture.io.ExchangeRateStore;
import software.ulpgc.moneycalculator.architecture.model.Currency;
import software.ulpgc.moneycalculator.architecture.model.ExchangeRate;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;

import static software.ulpgc.moneycalculator.application.webservice.ExchangeApi.*;
import static software.ulpgc.moneycalculator.application.webservice.GsonRateReader.readJsonIn;

public class WebServiceExchangeRateStore implements ExchangeRateStore {

    private final static String JsonConversionRateKey = "conversion_rates";

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
                readConversionRate(readJsonIn(date.isEqual(LocalDate.now()) ?
                                              currentUrl(from, to) :
                                              historicalUrl(date, from, to)))
        );
    }

    private URL currentUrl(Currency from, Currency to) throws MalformedURLException {
        return currentRateUrl(from, to);
    }

    private URL historicalUrl(LocalDate date, Currency from, Currency to) throws MalformedURLException {
        return historicalRateUrl(date, from, to);
    }

    private double readConversionRate(JsonObject object) {
        return isCurrentRate(object) ? object.get(JsonConversionRateKey).getAsDouble()
                : GsonRateReader.doubleIn(object.get("rates").getAsJsonObject());
    }

    private boolean isCurrentRate(JsonObject object) {
        return object.has(JsonConversionRateKey);
    }
}
