package com.aldisued.iot.monitoring.tasks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aldisued.iot.monitoring.IntegrationTestBase;
import com.aldisued.iot.monitoring.dto.SensorDto;
import com.aldisued.iot.monitoring.dto.SensorReadingDto;
import com.aldisued.iot.monitoring.entity.Sensor;
import com.aldisued.iot.monitoring.entity.SensorReading;
import com.aldisued.iot.monitoring.entity.SensorType;
import com.aldisued.iot.monitoring.repository.SensorReadingRepository;
import com.aldisued.iot.monitoring.repository.SensorRepository;
import com.aldisued.iot.monitoring.service.MeasurementService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

class AdditionalIntegrationTests extends IntegrationTestBase {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private SensorRepository sensorRepository;

  @Autowired
  private SensorReadingRepository sensorReadingRepository;

  @Autowired
  private MeasurementService measurementService;

  @AfterEach
  void cleanup() {
    sensorReadingRepository.deleteAll();
    sensorRepository.deleteAll();
  }

  @Test
  void rejectsWhitespaceOnlySensorName() throws Exception {
    var dto = new SensorDto("   ", SensorType.TEMPERATURE);

    mockMvc.perform(MockMvcRequestBuilders.post("/sensors")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void returnsNotFoundForReadingWithUnknownSensor() throws Exception {
    var dto = new SensorReadingDto(UUID.randomUUID(), 12.5, LocalDateTime.now());

    mockMvc.perform(MockMvcRequestBuilders.post("/sensor-readings")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)))
        .andExpect(status().isNotFound());
  }

  @Test
  void enforcesSensorNameUniquenessInDatabase() {
    sensorRepository.saveAndFlush(new Sensor("unique-name", SensorType.TEMPERATURE));

    assertThrows(
        DataIntegrityViolationException.class,
        () -> sensorRepository.saveAndFlush(new Sensor("unique-name", SensorType.HUMIDITY))
    );
  }

  @Test
  void includesBothTimeRangeBoundariesAndOrdersValues() {
    var sensor = sensorRepository.saveAndFlush(new Sensor("boundary-sensor", SensorType.TEMPERATURE));
    var from = LocalDateTime.parse("2026-01-01T00:00:00");
    var to = LocalDateTime.parse("2026-01-01T01:00:00");
    sensorReadingRepository.saveAllAndFlush(List.of(
        new SensorReading(5.0, from.minusSeconds(1), sensor),
        new SensorReading(10.0, from, sensor),
        new SensorReading(20.0, to, sensor)
    ));

    assertEquals(
        List.of(10.0, 20.0),
        measurementService.getMeasurementValuesBySensorType(SensorType.TEMPERATURE, from, to)
    );
    assertEquals(15.0, measurementService.getAverageTemperature(from, to).orElseThrow());
  }
}
