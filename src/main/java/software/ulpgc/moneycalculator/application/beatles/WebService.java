package software.ulpgc.moneycalculator.application.beatles;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import software.ulpgc.moneycalculator.architecture.io.CurrencyStore;
import software.ulpgc.moneycalculator.architecture.io.ExchangeRateStore;
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

        @Override
        public Stream<Currency> currencies() {
            try {
                return readCurrencies();
            } catch (IOException e) {
                return Stream.of();
            }
        }

        private Stream<Currency> readCurrencies() throws IOException {
            try (InputStream is = openInputStream(createConnection())) {
                return readCurrenciesWith(jsonIn(is));
            }
        }

        private Stream<Currency> readCurrenciesWith(String json) {
            return readCurrenciesWith(jsonObjectIn(json));
        }

        private Stream<Currency> readCurrenciesWith(JsonObject jsonObject) {
            return readCurrenciesWith(jsonObject.get("supported_codes").getAsJsonArray());
        }

        private Stream<Currency> readCurrenciesWith(JsonArray jsonArray) {
            List<Currency> list = new ArrayList<>();
            for (JsonElement item : jsonArray)
                list.add(readCurrencyWith(item.getAsJsonArray()));
            return list.stream();
        }

        private Currency readCurrencyWith(JsonArray tuple) {
            return new Currency(
                    tuple.get(0).getAsString(),
                    tuple.get(1).getAsString()
            );
        }

        private static String jsonIn(InputStream is) throws IOException {
            return new String(is.readAllBytes());
        }

        private static JsonObject jsonObjectIn(String json) {
            return new Gson().fromJson(json, JsonObject.class);
        }

        private InputStream openInputStream(URLConnection connection) throws IOException {
            return connection.getInputStream();
        }

        private static URLConnection createConnection() throws IOException {
            URL url = new URL((ExchangeRateApiUrl + "codes"));
            return url.openConnection();
        }
    }

    public static class HistoricalCurrencyStore implements software.ulpgc.moneycalculator.architecture.io.CurrencyStore {

        @Override
        public Stream<Currency> currencies() {
            try {
                return readCurrencies();
            } catch (IOException e) {
                return Stream.of();
            }
        }

        private Stream<Currency> readCurrencies() throws IOException {
            try (InputStream is = openInputStream(createConnection())) {
                return readCurrenciesWith(jsonIn(is));
            }
        }

        private Stream<Currency> readCurrenciesWith(String json) {
            return readCurrenciesWith(jsonObjectIn(json));
        }


        private Stream<Currency> readCurrenciesWith(JsonObject jsonObject) {
            return jsonObject.entrySet().stream().map(this::readCurrency);
        }

        private Currency readCurrency(Map.Entry<String, JsonElement> entry) {
            return readCurrencyWith(entry.getKey(), entry.getValue().getAsString());
        }

        private Currency readCurrencyWith(String code, String country) {
            return new Currency(code, country);
        }


        private static String jsonIn(InputStream is) throws IOException {
            return new String(is.readAllBytes());
        }

        private static JsonObject jsonObjectIn(String json) {
            return new Gson().fromJson(json, JsonObject.class);
        }

        private InputStream openInputStream(URLConnection connection) throws IOException {
            return connection.getInputStream();
        }

        private static URLConnection createConnection() throws IOException {
            URL url = new URL((HistoricalRatesApiUrl + "currencies"));
            return url.openConnection();
        }
    }
    
    public static class ExchangeRateStore implements software.ulpgc.moneycalculator.architecture.io.ExchangeRateStore {

        @Override
        public ExchangeRate load(Currency from, Currency to, LocalDate date) {
            try {
                return date.isEqual(LocalDate.now()) ? currentExchangeRate(from, to) : historicalExchangeRate(date, from, to);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private ExchangeRate currentExchangeRate(Currency from, Currency to) throws IOException {
            return new ExchangeRate(
                    LocalDate.now(),
                    from,
                    to,
                    readConversionRate(new URL(ExchangeRateApiUrl + "pair/" + from.code() + "/" + to.code()))
            );
        }

        private ExchangeRate historicalExchangeRate(LocalDate date, Currency from, Currency to) throws IOException {
            return new ExchangeRate(
                    date,
                    from,
                    to,
                    readConversionRate(new URL(HistoricalRatesApiUrl +
                            date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) +
                            "?base=" + from.code() +
                            "&symbols=" + to.code()))
            );
        }

        private double readConversionRate(URL url) throws IOException {
            return readConversionRate(url.openConnection());
        }

        private double readConversionRate(URLConnection urlConnection) throws IOException {
            try (InputStream inputStream = urlConnection.getInputStream()) {
                return readConversionRate(new String(new BufferedInputStream(inputStream).readAllBytes()));
            }
        }

        private double readConversionRate(String s) {
            return readConversionRate(new Gson().fromJson(s, JsonObject.class));
        }

        private double readConversionRate(JsonObject object) {
            return isCurrentRate(object) ? object.get(JsonConversionRateKey).getAsDouble()
                    : object.get("rates").getAsJsonObject()
                    .entrySet().iterator().next()
                    .getValue().getAsDouble();
        }

        private boolean isCurrentRate(JsonObject object) {
            return object.has(JsonConversionRateKey);
        }
    }
    public static class ExchangeRateSeriesStore implements software.ulpgc.moneycalculator.architecture.io.ExchangeRateSeriesStore {

        @Override
        public Stream<ExchangeRate> exchangeRatesBetween(Currency from, Currency to, LocalDate start, LocalDate end) {
            try {
                return ratesIn(new URL(HistoricalRatesApiUrl + start + ".." + end +
                        "?base=" + from.code() + "&symbols=" + to.code()))
                        .map(e -> createExchangeRateWith(from, to, e));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private Stream<Map.Entry<String, JsonElement>> ratesIn(URL url) throws IOException {
            return ratesIn(url.openConnection());
        }

        private Stream<Map.Entry<String, JsonElement>> ratesIn(URLConnection urlConnection) {
            try (InputStream inputStream = urlConnection.getInputStream()) {
                return ratesIn(new String(new BufferedInputStream(inputStream).readAllBytes()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private Stream<Map.Entry<String, JsonElement>> ratesIn(String json) {
            return ratesIn(new Gson().fromJson(json, JsonObject.class));
        }

        private double doubleIn(JsonObject jsonObject) {
            return jsonObject.entrySet().iterator().next().getValue().getAsDouble();
        }


        private ExchangeRate createExchangeRateWith(Currency from, Currency to, Map.Entry<String, JsonElement> e) {
            return new ExchangeRate(
                    LocalDate.parse(e.getKey()),
                    from,
                    to,
                    doubleIn(e.getValue().getAsJsonObject())
            );
        }

        private Stream<Map.Entry<String, JsonElement>> ratesIn(JsonObject jsonObject) {
            return jsonObject.get("rates").getAsJsonObject().entrySet().stream();
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
