package com.aldisued.iot.monitoring.service;

import com.aldisued.iot.monitoring.dto.SensorDto;
import com.aldisued.iot.monitoring.entity.Sensor;
import com.aldisued.iot.monitoring.exception.DuplicateSensorNameException;
import com.aldisued.iot.monitoring.repository.SensorRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class SensorService {

  private final SensorRepository sensorRepository;

  public SensorService(SensorRepository sensorRepository) {
    this.sensorRepository = sensorRepository;
  }

  public Sensor saveSensor(SensorDto sensor) {
    if (sensorRepository.existsByName(sensor.name())) {
      throw new DuplicateSensorNameException(sensor.name(), null);
    }

    try {
      return sensorRepository.saveAndFlush(new Sensor(
          sensor.name(),
          sensor.type()
      ));
    } catch (DataIntegrityViolationException exception) {
      throw new DuplicateSensorNameException(sensor.name(), exception);
    }
  }
}
