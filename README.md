# Service Jobs API

A small REST API for scheduling and tracking field-service jobs. It covers creating a job, assigning a technician, rescheduling, and moving the job through its lifecycle.

It's built with **Java 21, Spring Boot 4, Spring Data JPA, Bean Validation and H2**.

## What it shows

- A clean layered design, with a controller, a service, a repository and the domain model
- Business rules in the domain model. A job can only move `SCHEDULED → IN_PROGRESS → COMPLETED`, or be `CANCELLED` before it's completed. Only scheduled jobs can be rescheduled, and jobs that are in progress or completed can't be deleted.
- Input validation with field-level error messages
- Consistent error responses using RFC 9457 Problem Details (`400`, `404`, `409`)
- Unit tests and full HTTP integration tests

## Endpoints

| Method | Path | Purpose |
|---|---|---|
| POST | `/api/jobs` | Create a job (returns `201` + `Location`) |
| GET | `/api/jobs?status=&technician=&day=YYYY-MM-DD` | List jobs, with an optional filter |
| GET | `/api/jobs/{id}` | Get one job |
| PATCH | `/api/jobs/{id}/status` | Change status (`{"status":"IN_PROGRESS"}`) |
| PATCH | `/api/jobs/{id}/schedule` | Reschedule or reassign a scheduled job |
| DELETE | `/api/jobs/{id}` | Delete a scheduled or cancelled job |
| GET | `/actuator/health` | Health check |

## Run

```bash
./mvnw spring-boot:run
curl -X POST localhost:8080/api/jobs -H 'Content-Type: application/json' \
  -d '{"customerName":"Acme HVAC","description":"AC tune-up","technician":"Sam","scheduledFor":"2030-01-15T09:00:00","quotedPrice":129.00}'
```

## Test

```bash
./mvnw test
```

Data is kept in an in-memory H2 database, so every restart starts fresh. To use it for real, you would point `spring.datasource.*` at Postgres or MySQL and add migrations.
