package com.aldisued.iot.monitoring.controller;

import com.aldisued.iot.monitoring.exception.AlertNotFoundException;
import com.aldisued.iot.monitoring.exception.DuplicateSensorNameException;
import com.aldisued.iot.monitoring.exception.SensorNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

  @ExceptionHandler({SensorNotFoundException.class, AlertNotFoundException.class})
  public ProblemDetail handleNotFound(RuntimeException exception) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
  }

  @ExceptionHandler(DuplicateSensorNameException.class)
  public ProblemDetail handleDuplicateSensorName(DuplicateSensorNameException exception) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
  }
}
