package software.ulpgc.moneycalculator.application.custom;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import software.ulpgc.moneycalculator.architecture.model.Currency;
import software.ulpgc.moneycalculator.architecture.model.ExchangeRate;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class WebService {
    private static final String ExchangeRateApiUrl = "https://v6.exchangerate-api.com/v6/API-KEY/".replace("API-KEY", apiKey());
    private static final String JsonConversionRateKey = "conversion_rate";

    private static final String HistoricalRatesApiUrl = "https://api.frankfurter.dev/v1/";

    public static class CurrencyStore implements software.ulpgc.moneycalculator.architecture.io.CurrencyStore {

        private final String currenciesUrl;

        private CurrencyStore(String apiUrl, String resource) {
            this.currenciesUrl = apiUrl + resource;
        }

        public static CurrencyStore forCurrentCurrencies() {
            return new CurrencyStore(ExchangeRateApiUrl, "codes");
        }

        public static CurrencyStore forHistoricalCurrencies() {
            return new CurrencyStore(HistoricalRatesApiUrl, "currencies");
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
            return readCurrenciesIn(RateReader.readJsonIn(new URL(currenciesUrl)));
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

    public static class ExchangeRateStore implements software.ulpgc.moneycalculator.architecture.io.ExchangeRateStore {

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
                    readConversionRate(RateReader.readJsonIn(date.isEqual(LocalDate.now()) ?
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
                    : RateReader.doubleIn(object.get("rates").getAsJsonObject());
        }

        private boolean isCurrentRate(JsonObject object) {
            return object.has(JsonConversionRateKey);
        }
    }

    public static class ExchangeRateSeriesStore implements software.ulpgc.moneycalculator.architecture.io.ExchangeRateSeriesStore {

        @Override
        public Stream<ExchangeRate> exchangeRatesBetween(Currency from, Currency to, LocalDate start, LocalDate end) {
            try {
                return ratesIn(RateReader.readJsonIn(urlFrom(from, to, start, end)))
                        .map(e -> createExchangeRateWith(from, to, e));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private ExchangeRate createExchangeRateWith(Currency from, Currency to, Map.Entry<String, JsonElement> e) {
            return new ExchangeRate(
                    LocalDate.parse(e.getKey()),
                    from,
                    to,
                    RateReader.doubleIn(e.getValue().getAsJsonObject())
            );
        }

        private Stream<Map.Entry<String, JsonElement>> ratesIn(JsonObject jsonObject) {
            return jsonObject.get("rates").getAsJsonObject().entrySet().stream();
        }

        private URL urlFrom(Currency from, Currency to, LocalDate start, LocalDate end) throws MalformedURLException {
            return new URL(HistoricalRatesApiUrl + start + ".." + end +
                    "?base=" + from.code() + "&symbols=" + to.code());
        }

    }

    public static class RateReader {

        private static final Gson gson = new Gson();

        public static JsonObject readJsonIn(URL url) throws IOException {
            return readJsonIn(url.openConnection());
        }

        private static JsonObject readJsonIn(URLConnection urlConnection) throws IOException {
            try (InputStream inputStream = urlConnection.getInputStream()) {
                return readJsonIn(new String(new BufferedInputStream(inputStream).readAllBytes()));
            }
        }

        private static JsonObject readJsonIn(String json) {
            return gson.fromJson(json, JsonObject.class);
        }

        public static double doubleIn(JsonObject object) {
            return object.entrySet().iterator().next().getValue().getAsDouble();
        }
    }

    private static String apiKey() {
        try (InputStream is = WebService.class.getResourceAsStream("/api-key.txt")) {
            return new String(new BufferedInputStream(is).readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
