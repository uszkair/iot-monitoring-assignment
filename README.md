# IoT Sensor Monitoring Application

A Spring Boot backend for collecting and analyzing temperature, humidity, and atmospheric
pressure readings. Sensor readings and alerts can arrive over HTTP or Kafka and are persisted in
PostgreSQL.

This repository contains the completed implementation of all tasks in [`TASKS.md`](TASKS.md),
including the optional Kafka publishing, typed measurement queries, outlier filtering, and moving
average calculation tasks.

## Technology stack

- Java 21
- Spring Boot 3.5
- Spring Data JPA / Hibernate
- PostgreSQL 17
- Spring Kafka
- Maven Wrapper
- JUnit 5 and Testcontainers

## Prerequisites

- JDK 21
- Docker Desktop or another Docker-compatible runtime

The automated integration tests start an isolated PostgreSQL 17 container. No manually configured
database is required for the test suite.

## Build and test

On Windows:

```powershell
.\mvnw.cmd test
```

On Linux or macOS:

```bash
./mvnw test
```

To run one assignment test class during development:

```powershell
.\mvnw.cmd -Dtest=Task5Tests test
```

## Run locally

Start PostgreSQL:

```powershell
docker compose up -d postgresql
```

Configure the datasource and start the application:

```powershell
$env:SPRING_DATASOURCE_URL = "jdbc:postgresql://localhost:5432/monitoring"
$env:SPRING_DATASOURCE_USERNAME = "monitoring_user"
$env:SPRING_DATASOURCE_PASSWORD = "IOT123!"
.\mvnw.cmd spring-boot:run
```

The API is available at `http://localhost:8080`.

## API examples

Create a sensor:

```bash
curl -X POST http://localhost:8080/sensors \
  -H "Content-Type: application/json" \
  -d '{"name":"Office temperature","type":"TEMPERATURE"}'
```

Save a sensor reading:

```bash
curl -X POST http://localhost:8080/sensor-readings \
  -H "Content-Type: application/json" \
  -d '{"sensorId":"777d727e-6650-415f-85eb-9c9ca05f65c1","value":22.8,"timestamp":"2026-10-27T14:30:00"}'
```

Retrieve the latest alert for a sensor:

```bash
curl "http://localhost:8080/alerts/latest?sensorId=777d727e-6650-415f-85eb-9c9ca05f65c1"
```

## Implemented assignment tasks

1. Store sensor enums as readable strings.
2. Map bidirectional sensor-reading and sensor-alert relationships.
3. Persist readings received through HTTP or Kafka.
4. Validate sensor creation and return `409 Conflict` for duplicate names.
5. Return the latest alert through `GET /alerts/latest`.
6. Persist alerts and publish their DTOs to the `alerts` Kafka topic.
7. Calculate average temperature for an inclusive time range.
8. Return timestamp-ordered values by sensor type and time range.
9. Filter values by their allowed deviation from the average.
10. Calculate moving averages with validated window sizes.

Database tables are intentionally generated from the JPA entities, as required by the assignment.
