package com.aldisued.iot.monitoring.tasks;

import static org.assertj.core.api.Assertions.assertThat;

import com.aldisued.iot.monitoring.dto.AlertDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.UUID;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.converter.StringJsonMessageConverter;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

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
  void convertsOutgoingDtoToJson() throws Exception {
    AlertDto alertDto = new AlertDto(
        UUID.randomUUID(), "Temperature is too high", LocalDateTime.now());

    ProducerRecord<?, ?> record = converter.fromMessage(
        MessageBuilder.withPayload(alertDto).build(), "alerts");

    assertThat(record).isNotNull();
    assertThat(record.value()).isInstanceOf(String.class);
    assertThat(objectMapper.readValue((String) record.value(), AlertDto.class))
        .isEqualTo(alertDto);
  }
}
