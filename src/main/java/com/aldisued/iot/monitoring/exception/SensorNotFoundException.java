package com.aldisued.iot.monitoring.exception;

import java.util.UUID;

public class SensorNotFoundException extends RuntimeException {

  public SensorNotFoundException(UUID sensorId) {
    super("Sensor not found: " + sensorId);
  }
}
