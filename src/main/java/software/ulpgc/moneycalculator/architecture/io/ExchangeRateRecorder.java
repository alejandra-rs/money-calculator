package software.ulpgc.moneycalculator.architecture.io;

import software.ulpgc.moneycalculator.architecture.model.ExchangeRate;

import java.util.stream.Stream;

public interface ExchangeRateRecorder {
    void record(Stream<ExchangeRate> exchangeRates);
}
