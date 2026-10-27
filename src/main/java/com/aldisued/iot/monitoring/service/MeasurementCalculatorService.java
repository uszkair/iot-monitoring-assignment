package com.aldisued.iot.monitoring.service;


import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MeasurementCalculatorService {

  public List<Double> filterByAverageDeviation(List<Double> values, Double deviation) {
    if (deviation == null || !Double.isFinite(deviation) || deviation < 0.0 || deviation > 1.0) {
      throw new IllegalArgumentException("Deviation must be between 0.0 and 1.0");
    }
    if (values == null) {
      throw new IllegalArgumentException("Values must not be null");
    }
    if (values.isEmpty()) {
      return List.of();
    }

    double average = values.stream().mapToDouble(Double::doubleValue).average().orElseThrow();
    double tolerance = Math.abs(average) * deviation;
    double lowerBound = average - tolerance;
    double upperBound = average + tolerance;

    return values.stream()
        .filter(value -> value >= lowerBound && value <= upperBound)
        .toList();
  }

  public List<Double> getMovingAverage(List<Double> data, int windowSize) {
    if (data == null) {
      throw new IllegalArgumentException("Data must not be null");
    }
    if (windowSize <= 0 || windowSize > data.size()) {
      throw new IllegalArgumentException(
          "Window size must be positive and no greater than data size");
    }

    var result = new ArrayList<Double>(data.size() - windowSize + 1);
    double windowSum = 0.0;
    for (int i = 0; i < windowSize; i++) {
      windowSum += data.get(i);
    }
    result.add(windowSum / windowSize);

    for (int i = windowSize; i < data.size(); i++) {
      windowSum += data.get(i) - data.get(i - windowSize);
      result.add(windowSum / windowSize);
    }
    return result;
  }

}
