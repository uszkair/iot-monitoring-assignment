package com.aldisued.iot.monitoring.repository;

import com.aldisued.iot.monitoring.entity.SensorReading;
import com.aldisued.iot.monitoring.entity.SensorType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SensorReadingRepository extends JpaRepository<SensorReading, Long> {

  @Query("""
      select avg(reading.value)
      from SensorReading reading
      where reading.sensor.type = :sensorType
        and reading.timestamp between :from and :to
      """)
  Optional<Double> findAverageBySensorTypeAndTimestampBetween(
      @Param("sensorType") SensorType sensorType,
      @Param("from") LocalDateTime from,
      @Param("to") LocalDateTime to);

  @Query("""
      select reading.value
      from SensorReading reading
      where reading.sensor.type = :sensorType
        and reading.timestamp between :from and :to
      order by reading.timestamp asc
      """)
  List<Double> findValuesBySensorTypeAndTimestampBetweenOrderByTimestamp(
      @Param("sensorType") SensorType sensorType,
      @Param("from") LocalDateTime from,
      @Param("to") LocalDateTime to);
}
