package com.aldisued.iot.monitoring.service;

import com.aldisued.iot.monitoring.dto.AlertDto;
import com.aldisued.iot.monitoring.entity.Alert;
import com.aldisued.iot.monitoring.repository.AlertRepository;
import com.aldisued.iot.monitoring.repository.SensorRepository;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AlertService {

  private final AlertRepository alertRepository;
  private final SensorRepository sensorRepository;
  private final KafkaTemplate<String, AlertDto> kafkaTemplate;

  public AlertService(AlertRepository alertRepository, SensorRepository sensorRepository,
      KafkaTemplate<String, AlertDto> kafkaTemplate) {
    this.alertRepository = alertRepository;
    this.sensorRepository = sensorRepository;
    this.kafkaTemplate = kafkaTemplate;
  }

  public Alert saveAlert(AlertDto alertDto) {
    var sensor = sensorRepository.findById(alertDto.sensorId())
        .orElseThrow(() -> sensorNotFound(alertDto.sensorId()));
    var alert = alertRepository.save(new Alert(
        alertDto.message(),
        alertDto.timestamp(),
        sensor
    ));

    kafkaTemplate.send("alerts", alertDto);
    return alert;
  }

  public AlertDto findLastAlertBySensorId(UUID sensorId) {
    return alertRepository.findFirstBySensorIdOrderByTimestampDesc(sensorId)
        .map(alert -> new AlertDto(
            alert.getSensor().getId(),
            alert.getMessage(),
            alert.getTimestamp()
        ))
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "No alert found for sensor: " + sensorId
        ));
  }

  private ResponseStatusException sensorNotFound(UUID sensorId) {
    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Sensor not found: " + sensorId);
  }
}
