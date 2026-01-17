package software.ulpgc.moneycalculator.application.custom;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class WebService {

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

     public static String apiKey() {
        try (InputStream is = WebService.class.getResourceAsStream("/api-key.txt")) {
            return new String(new BufferedInputStream(is).readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
