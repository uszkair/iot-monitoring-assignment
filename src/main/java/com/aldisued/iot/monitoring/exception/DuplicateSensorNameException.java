package com.aldisued.iot.monitoring.exception;

public class DuplicateSensorNameException extends RuntimeException {

  public DuplicateSensorNameException(String name, Throwable cause) {
    super("A sensor named '%s' already exists".formatted(name), cause);
  }
}
