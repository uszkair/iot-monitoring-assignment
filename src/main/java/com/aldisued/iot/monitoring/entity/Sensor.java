package com.aldisued.iot.monitoring.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Table(name = "sensors")
@Entity
public class Sensor {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, unique = true)
  private String name;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private SensorType type;

  @JsonIgnore
  @OneToMany(mappedBy = "sensor")
  private List<Alert> alerts = new ArrayList<>();

  @JsonIgnore
  @OneToMany(mappedBy = "sensor")
  private List<SensorReading> sensorReadings = new ArrayList<>();

  public Sensor() {}

  public Sensor(String name, SensorType type) {
    this.name = name;
    this.type = type;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public SensorType getType() {
    return type;
  }

  public void setType(SensorType type) {
    this.type = type;
  }

  public List<Alert> getAlerts() {
    return alerts;
  }

  public void setAlerts(List<Alert> alerts) {
    this.alerts = alerts;
  }

  public List<SensorReading> getSensorReadings() {
    return sensorReadings;
  }

  public void setSensorReadings(
      List<SensorReading> sensorReadings) {
    this.sensorReadings = sensorReadings;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Sensor sensor = (Sensor) o;
    return Objects.equals(id, sensor.id) && Objects.equals(name, sensor.name)
        && type == sensor.type;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, type);
  }
}
