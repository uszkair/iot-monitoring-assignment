package com.aldisued.iot.monitoring.config;

import com.aldisued.iot.monitoring.exception.SensorNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.kafka.DefaultKafkaProducerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListenerConfigurer;
import org.springframework.kafka.config.KafkaListenerEndpointRegistrar;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.converter.RecordMessageConverter;
import org.springframework.kafka.support.converter.StringJsonMessageConverter;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@Configuration
public class KafkaConfiguration implements KafkaListenerConfigurer {

  private final LocalValidatorFactoryBean validator;

  public KafkaConfiguration(LocalValidatorFactoryBean validator) {
    this.validator = validator;
  }

  @Bean
  public RecordMessageConverter kafkaMessageConverter(ObjectMapper objectMapper) {
    return new StringJsonMessageConverter(objectMapper);
  }

  /**
   * Serializes outgoing values with the application's ObjectMapper, so published messages use
   * the same JSON format (e.g. ISO-8601 timestamps) as the HTTP API and incoming messages.
   */
  @Bean
  @SuppressWarnings("unchecked")
  public DefaultKafkaProducerFactoryCustomizer jsonValueSerializerCustomizer(
      ObjectMapper objectMapper) {
    return producerFactory -> ((DefaultKafkaProducerFactory<Object, Object>) producerFactory)
        .setValueSerializerSupplier(() -> new JsonSerializer<>(objectMapper));
  }

  /**
   * Retrying cannot fix a message that references an unknown sensor. Invalid payloads
   * (bean validation, conversion errors) are already classified as not retryable by default.
   */
  @Bean
  public DefaultErrorHandler kafkaErrorHandler() {
    var errorHandler = new DefaultErrorHandler();
    errorHandler.addNotRetryableExceptions(SensorNotFoundException.class);
    return errorHandler;
  }

  @Override
  public void configureKafkaListeners(KafkaListenerEndpointRegistrar registrar) {
    registrar.setValidator(validator);
  }
}
