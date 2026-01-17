package software.ulpgc.moneycalculator.application.webservice;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import software.ulpgc.moneycalculator.architecture.io.CurrencyStore;
import software.ulpgc.moneycalculator.architecture.model.Currency;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static software.ulpgc.moneycalculator.application.webservice.ExchangeApi.*;

public class WebServiceCurrencyStore implements CurrencyStore {

    private final URL currenciesUrl;

    private WebServiceCurrencyStore(URL url) {
        this.currenciesUrl = url;
    }

    public static WebServiceCurrencyStore forCurrentCurrencies() throws MalformedURLException {
        return new WebServiceCurrencyStore(currentCurrenciesUrl());
    }

    public static WebServiceCurrencyStore forHistoricalCurrencies() throws MalformedURLException {
        return new WebServiceCurrencyStore(historicalCurrenciesUrl());
    }

    @Override
    public Stream<Currency> currencies() {
        try {
            return readCurrencies();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Stream<Currency> readCurrencies() throws IOException {
        return readCurrenciesIn(GsonRateReader.readJsonIn(currenciesUrl));
    }


    private Stream<Currency> readCurrenciesIn(JsonObject jsonObject) {
        return readingCurrentCurrencies(jsonObject) ? parseCurrentCurrencies(jsonObject)
                : parseHistoricalCurrencies(jsonObject);
    }

    private boolean readingCurrentCurrencies(JsonObject jsonObject) {
        return jsonObject.has("supported_codes");
    }

    private Stream<Currency> parseCurrentCurrencies(JsonObject jsonObject) {
        return readCurrenciesIn(jsonObject.get("supported_codes").getAsJsonArray());
    }

    private Stream<Currency> parseHistoricalCurrencies(JsonObject jsonObject) {
        return jsonObject.entrySet().stream().map(this::readCurrency);
    }

    private Currency readCurrency(Map.Entry<String, JsonElement> entry) {
        return readCurrencyWith(entry.getKey(), entry.getValue().getAsString());
    }

    private Stream<Currency> readCurrenciesIn(JsonArray jsonArray) {
        List<Currency> list = new ArrayList<>();
        for (JsonElement item : jsonArray)
            list.add(readCurrencyWith(item.getAsJsonArray()));
        return list.stream();
    }

    private Currency readCurrencyWith(JsonArray tuple) {
        return readCurrencyWith(tuple.get(0).getAsString(),
                tuple.get(1).getAsString());
    }

    private Currency readCurrencyWith(String code, String country) {
        return new Currency(code, country);
    }
}
