# BookFlow API

BookFlow API is a learning-focused, production-minded Spring Boot backend for booking and staff availability workflows. It simulates a SaaS platform for service-based businesses such as clinics, salons, training centers, consultants, repair shops, and professional service providers.

## Project Goal

Build a clean, secure REST API that helps businesses manage:

- Business profiles, services, staff, and availability
- Customer bookings and booking status workflows
- Reminders, notifications, audit logs, reports, and dashboards

## Learning Goals

This project is designed to practice:

- Java 21, Spring Boot, REST APIs, DTOs, and validation
- Global exception handling and standard response shapes
- Spring Data JPA, PostgreSQL, Flyway, transactions, and locking
- Spring Security, JWT, refresh tokens, RBAC, and ownership checks
- Domain events, Redis caching, scheduled reminders, and audit logging
- OpenAPI, Docker, Actuator, JUnit, Mockito, MockMvc, and Testcontainers

## Architecture Direction

BookFlow should start as a modular monolith with layered architecture. Recommended package direction:

```text
com.bookflow
├── auth
├── users
├── businesses
├── services
├── staff
├── availability
├── bookings
├── notifications
├── audit
├── reports
├── common
├── config
└── security
```

Each module should own its controllers, DTOs, entities, repositories, mappers, and services.

## Current Status

Sprint 0 foundation is in place:

- Maven wrapper and Spring Boot application class
- Java package root: `com.bookflow`
- Feature package skeleton for the modular monolith
- Profile config: `dev`, `test`, and `prod`
- PostgreSQL and Redis Docker Compose services
- Actuator health and OpenAPI/Swagger dependencies
- Basic context-load test
- Requirements and design documents in `docs/`

The `docs/` folder is local reference material and does not need to be pushed.

## Local Setup

Requirements:

- Java 21
- Docker and Docker Compose
- Maven wrapper from this repo

## Local Commands

```bash
docker compose up -d
./mvnw test
./mvnw spring-boot:run
```

If a local port is already busy:

```bash
BOOKFLOW_POSTGRES_PORT=5433 BOOKFLOW_REDIS_PORT=6380 docker compose up -d
BOOKFLOW_DB_URL=jdbc:postgresql://localhost:5433/bookflow BOOKFLOW_REDIS_PORT=6380 ./mvnw spring-boot:run
```

If app port `8080` is busy, add a free port such as `SERVER_PORT=18080` to the run command.

Useful URLs:

- Health: `http://localhost:8080/actuator/health`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

## First Implementation Target

Start with Sprint 1: common backend foundation.

Focus on standard responses, error handling, validation formatting, base exceptions, pagination, and Swagger/OpenAPI setup.
