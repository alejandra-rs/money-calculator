package software.ulpgc.moneycalculator.application.webservice;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import software.ulpgc.moneycalculator.architecture.io.ExchangeRateSeriesStore;
import software.ulpgc.moneycalculator.architecture.model.Currency;
import software.ulpgc.moneycalculator.architecture.model.ExchangeRate;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Stream;

import static software.ulpgc.moneycalculator.application.webservice.ExchangeApi.seriesUrl;

public class WebServiceExchangeRateSeriesStore implements ExchangeRateSeriesStore {

    @Override
    public Stream<ExchangeRate> exchangeRatesBetween(Currency from, Currency to, LocalDate start, LocalDate end) {
        try {
            return ratesIn(GsonRateReader.readJsonIn(urlFrom(from, to, start, end)))
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
                GsonRateReader.doubleIn(e.getValue().getAsJsonObject())
        );
    }

    private Stream<Map.Entry<String, JsonElement>> ratesIn(JsonObject jsonObject) {
        return jsonObject.get("rates").getAsJsonObject().entrySet().stream();
    }

    private URL urlFrom(Currency from, Currency to, LocalDate start, LocalDate end) throws MalformedURLException {
        return seriesUrl(from, to, start, end);
    }

}
