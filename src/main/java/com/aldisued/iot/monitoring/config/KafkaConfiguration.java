package com.aldisued.iot.monitoring.config;

import com.aldisued.iot.monitoring.exception.SensorNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListenerConfigurer;
import org.springframework.kafka.config.KafkaListenerEndpointRegistrar;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.converter.RecordMessageConverter;
import org.springframework.kafka.support.converter.StringJsonMessageConverter;
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
