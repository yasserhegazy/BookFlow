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

The repository currently contains:

- Maven wrapper, Spring Boot application class, and `application.properties`
- Basic context-load test
- Requirements and design documents in `docs/`

The current `pom.xml` includes Spring Web, Validation, DevTools, and Spring Boot Test.

## Important Setup Notes

Before deeper implementation, align setup with the sprint plan:

- Add JPA, PostgreSQL, Flyway, Security, Actuator, Redis, OpenAPI, and Testcontainers
- Create the base feature package structure
- Decide whether `docs/` should stay ignored by Git

## Local Commands

```bash
./mvnw test
./mvnw spring-boot:run
```

## First Implementation Target

Start with Sprint 1: common backend foundation.

Focus on standard responses, error handling, validation formatting, base exceptions, pagination, and Swagger/OpenAPI setup.
