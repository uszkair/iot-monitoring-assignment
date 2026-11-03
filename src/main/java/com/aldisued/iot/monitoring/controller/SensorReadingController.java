package com.aldisued.iot.monitoring.controller;

import com.aldisued.iot.monitoring.dto.SensorReadingDto;
import com.aldisued.iot.monitoring.entity.SensorReading;
import com.aldisued.iot.monitoring.service.SensorReadingService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sensor-readings")
public class SensorReadingController {

  private final SensorReadingService sensorReadingService;

  public SensorReadingController(SensorReadingService sensorReadingService) {
    this.sensorReadingService = sensorReadingService;
  }

  @PostMapping
  public SensorReading saveSensorReading(@Valid @RequestBody SensorReadingDto sensorReadingDto) {
    return sensorReadingService.saveSensorReading(sensorReadingDto);
  }
}
