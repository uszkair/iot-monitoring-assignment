package com.aldisued.iot.monitoring.service;

import com.aldisued.iot.monitoring.dto.SensorDto;
import com.aldisued.iot.monitoring.entity.Sensor;
import com.aldisued.iot.monitoring.repository.SensorRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SensorService {

  private final SensorRepository sensorRepository;

  public SensorService(SensorRepository sensorRepository) {
    this.sensorRepository = sensorRepository;
  }

  public Sensor saveSensor(SensorDto sensor) {
    if (sensorRepository.existsByName(sensor.name())) {
      throw duplicateSensorName(sensor.name(), null);
    }

    try {
      return sensorRepository.saveAndFlush(new Sensor(
          sensor.name(),
          sensor.type()
      ));
    } catch (DataIntegrityViolationException exception) {
      throw duplicateSensorName(sensor.name(), exception);
    }
  }

  private ResponseStatusException duplicateSensorName(String name, Throwable cause) {
    return new ResponseStatusException(
        HttpStatus.CONFLICT,
        "A sensor named '%s' already exists".formatted(name),
        cause
    );
  }
}
