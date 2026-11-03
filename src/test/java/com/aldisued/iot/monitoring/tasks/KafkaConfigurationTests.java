package com.aldisued.iot.monitoring.tasks;

import static org.assertj.core.api.Assertions.assertThat;

import com.aldisued.iot.monitoring.dto.AlertDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Properties;
import java.util.UUID;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.Serializer;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.support.converter.StringJsonMessageConverter;
import org.springframework.messaging.Message;

class KafkaConfigurationTests {

  private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
  private final StringJsonMessageConverter converter =
      new StringJsonMessageConverter(objectMapper);

  @Test
  void convertsIncomingJsonToListenerDto() throws Exception {
    AlertDto alertDto = new AlertDto(
        UUID.randomUUID(), "Temperature is too high", LocalDateTime.now());
    String json = objectMapper.writeValueAsString(alertDto);
    ConsumerRecord<String, String> record =
        new ConsumerRecord<>("sensor-alerts", 0, 0, null, json);

    Message<?> message = converter.toMessage(record, null, null, AlertDto.class);

    assertThat(message.getPayload()).isEqualTo(alertDto);
  }

  @Test
  void configuredProducerSerializerConvertsOutgoingDtoToJson() throws Exception {
    AlertDto alertDto = new AlertDto(
        UUID.randomUUID(), "Temperature is too high", LocalDateTime.now());
    Serializer<AlertDto> serializer = new JsonSerializer<>(objectMapper);

    byte[] json = serializer.serialize("alerts", alertDto);

    assertThat(objectMapper.readValue(json, AlertDto.class))
        .isEqualTo(alertDto);
  }

  @Test
  void applicationConfigSelectsJsonSerializerForKafkaProducer() throws IOException {
    Properties properties = new Properties();
    try (InputStream input = getClass().getClassLoader()
        .getResourceAsStream("application.properties")) {
      assertThat(input).isNotNull();
      properties.load(input);
    }

    assertThat(properties.getProperty("spring.kafka.producer.value-serializer"))
        .isEqualTo(JsonSerializer.class.getName());
  }
}
