package software.ulpgc.moneycalculator.architecture.viewmodel;

import java.time.LocalDate;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class LineChart implements Iterable<LocalDate> {
    private final Map<LocalDate, Double> timeSeries;

    public LineChart() {
        this.timeSeries = new TreeMap<>();
    }

    public void add(LocalDate date, double rate) {
        timeSeries.put(date, rate);
    }

    public Double get(LocalDate key) {
        return timeSeries.get(key);
    }

    public int size() {
        return timeSeries.size();
    }

    @Override
    public Iterator<LocalDate> iterator() {
        return timeSeries.keySet().iterator();
    }
}
