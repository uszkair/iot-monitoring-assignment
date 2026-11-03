package com.aldisued.iot.monitoring.messaging;

import com.aldisued.iot.monitoring.dto.AlertDto;
import com.aldisued.iot.monitoring.service.AlertService;
import jakarta.validation.Valid;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class AlertKafkaListener {
  private final AlertService alertService;

  public AlertKafkaListener(AlertService alertService) {
    this.alertService = alertService;
  }

  @KafkaListener(topics = {"sensor-alerts"}, groupId = "iot-monitoring")
  public void listen(@Payload @Valid AlertDto alertDto) {
    alertService.saveAlert(alertDto);
  }

}
