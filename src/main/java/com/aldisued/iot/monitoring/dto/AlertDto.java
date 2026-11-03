package com.aldisued.iot.monitoring.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

public record AlertDto(
    @NotNull UUID sensorId,
    @NotBlank String message,
    @NotNull LocalDateTime timestamp
) {
}
