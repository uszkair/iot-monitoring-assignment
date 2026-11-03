package com.aldisued.iot.monitoring.tasks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.aldisued.iot.monitoring.IntegrationTestBase;
import com.aldisued.iot.monitoring.KafkaTestcontainersConfiguration;
import com.aldisued.iot.monitoring.dto.AlertDto;
import com.aldisued.iot.monitoring.dto.SensorReadingDto;
import com.aldisued.iot.monitoring.entity.Sensor;
import com.aldisued.iot.monitoring.entity.SensorType;
import com.aldisued.iot.monitoring.repository.AlertRepository;
import com.aldisued.iot.monitoring.repository.SensorReadingRepository;
import com.aldisued.iot.monitoring.repository.SensorRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.TestPropertySource;

@Import(KafkaTestcontainersConfiguration.class)
@TestPropertySource(properties = "spring.kafka.consumer.auto-offset-reset=earliest")
class KafkaIntegrationTests extends IntegrationTestBase {

  private static final Duration TIMEOUT = Duration.ofSeconds(30);

  @Autowired
  private KafkaTemplate<String, Object> kafkaTemplate;

  @Autowired
  private SensorRepository sensorRepository;

  @Autowired
  private SensorReadingRepository sensorReadingRepository;

  @Autowired
  private AlertRepository alertRepository;

  @Autowired
  private AlertsTopicCollector alertsTopicCollector;

  @Autowired
  private ObjectMapper objectMapper;

  private Sensor sensor;

  @BeforeEach
  void setup() {
    sensor = sensorRepository.saveAndFlush(new Sensor("kafka-sensor", SensorType.TEMPERATURE));
  }

  @AfterEach
  void cleanup() {
    sensorReadingRepository.deleteAll();
    alertRepository.deleteAll();
    sensorRepository.deleteAll();
    alertsTopicCollector.received.clear();
  }

  @Test
  void persistsReadingReceivedFromKafka() {
    var timestamp = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);

    kafkaTemplate.send("sensor-reading", new SensorReadingDto(sensor.getId(), 21.5, timestamp));

    await().atMost(TIMEOUT).untilAsserted(() -> assertThat(sensorReadingRepository.findAll())
        .singleElement()
        .satisfies(reading -> {
          assertThat(reading.getValue()).isEqualTo(21.5);
          assertThat(reading.getTimestamp()).isEqualTo(timestamp);
        }));
  }

  @Test
  void persistsAlertReceivedFromKafkaAndPublishesItToAlertsTopic() throws Exception {
    var alertDto = new AlertDto(
        sensor.getId(), "Too hot", LocalDateTime.parse("2026-10-27T14:31:00"));

    kafkaTemplate.send("sensor-alerts", alertDto);

    var published = alertsTopicCollector.received.poll(TIMEOUT.toSeconds(), TimeUnit.SECONDS);
    assertThat(published).isNotNull();
    assertThat(objectMapper.readTree(published).get("timestamp").asText())
        .isEqualTo("2026-10-27T14:31:00");
    assertThat(objectMapper.readValue(published, AlertDto.class)).isEqualTo(alertDto);
    assertThat(alertRepository.findAll())
        .singleElement()
        .satisfies(alert -> assertThat(alert.getMessage()).isEqualTo("Too hot"));
  }

  @Test
  void skipsInvalidAndUnknownSensorMessagesAndKeepsConsuming() {
    var timestamp = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);

    kafkaTemplate.send("sensor-reading", new SensorReadingDto(UUID.randomUUID(), 1.0, timestamp));
    kafkaTemplate.send("sensor-reading", new SensorReadingDto(sensor.getId(), null, timestamp));
    kafkaTemplate.send("sensor-reading", new SensorReadingDto(sensor.getId(), 3.0, timestamp));

    await().atMost(TIMEOUT).untilAsserted(() -> assertThat(sensorReadingRepository.findAll())
        .singleElement()
        .satisfies(reading -> assertThat(reading.getValue()).isEqualTo(3.0)));
  }

  @TestConfiguration(proxyBeanMethods = false)
  static class AlertsTopicCollectorConfiguration {

    @Bean
    AlertsTopicCollector alertsTopicCollector() {
      return new AlertsTopicCollector();
    }
  }

  static class AlertsTopicCollector {

    private final BlockingQueue<String> received = new LinkedBlockingQueue<>();

    @KafkaListener(topics = "alerts", groupId = "alerts-topic-test")
    void listen(ConsumerRecord<String, String> record) {
      received.add(record.value());
    }
  }
}
