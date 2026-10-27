package com.aldisued.iot.monitoring.tasks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.aldisued.iot.monitoring.service.MeasurementCalculatorService;
import java.util.List;
import org.junit.jupiter.api.Test;

class AdditionalCalculatorTests {

  private final MeasurementCalculatorService service = new MeasurementCalculatorService();

  @Test
  void keepsInclusiveDeviationBoundaries() {
    assertEquals(
        List.of(8.0, 10.0, 12.0),
        service.filterByAverageDeviation(List.of(8.0, 10.0, 12.0), 0.2)
    );
  }

  @Test
  void handlesNegativeAverage() {
    assertEquals(
        List.of(-10.0),
        service.filterByAverageDeviation(List.of(-12.0, -10.0, -8.0), 0.0)
    );
  }

  @Test
  void rejectsNonFiniteDeviation() {
    assertThrows(
        IllegalArgumentException.class,
        () -> service.filterByAverageDeviation(List.of(1.0), Double.NaN)
    );
  }

  @Test
  void acceptsWindowEqualToDataSize() {
    assertEquals(List.of(4.0), service.getMovingAverage(List.of(2.0, 4.0, 6.0), 3));
  }
}
