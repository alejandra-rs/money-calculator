package software.ulpgc.moneycalculator.application.webservice;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import software.ulpgc.moneycalculator.application.custom.WebService;
import software.ulpgc.moneycalculator.architecture.io.CurrencyStore;
import software.ulpgc.moneycalculator.architecture.model.Currency;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static software.ulpgc.moneycalculator.application.custom.WebService.apiKey;

public class WebServiceCurrencyStore implements CurrencyStore {

    private static final String ExchangeRateApiUrl = "https://v6.exchangerate-api.com/v6/API-KEY/".replace("API-KEY", apiKey());
    private static final String HistoricalRatesApiUrl = "https://api.frankfurter.dev/v1/";

    private final String currenciesUrl;

    private WebServiceCurrencyStore(String apiUrl, String resource) {
        this.currenciesUrl = apiUrl + resource;
    }

    public static WebServiceCurrencyStore forCurrentCurrencies() {
        return new WebServiceCurrencyStore(ExchangeRateApiUrl, "codes");
    }

    public static WebServiceCurrencyStore forHistoricalCurrencies() {
        return new WebServiceCurrencyStore(HistoricalRatesApiUrl, "currencies");
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
        return readCurrenciesIn(WebService.RateReader.readJsonIn(new URL(currenciesUrl)));
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
