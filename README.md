# Flight Booking Microservices System

A production-grade, event-driven microservices ecosystem built with **Spring Boot**, **Spring Cloud**, **MongoDB**, **RabbitMQ**, **JWT**, and **Docker**.  
The platform supports **authentication**, **flight search**, **booking**, **ticketing**, **cancellation**, **history**, and **notifications**—all running in a **distributed scalable architecture**.

---

## System Architecture

![system architecture.png]

---

## Microservices & Ports

| Service                  | Description                                  | Port           |
| ------------------------ | -------------------------------------------- | -------------- |
| **API Gateway**          | Single entry point for all external requests | `9090`         |
| **Eureka Server**        | Service discovery for all microservices      | `8761`         |
| **Config Server**        | Centralized configuration (Git-based)        | `8888`         |
| **Auth Service**         | Signup, login, JWT generation                | `8090`         |
| **Flight Service**       | Flight search, airline inventory             | `8081`         |
| **Booking Service**      | Booking, cancellation, history               | `8082`         |
| **Notification Service** | RabbitMQ consumer → email/log notifications  | `8083`         |
| **MongoDB**              | Databases for all services                   | `27017`        |
| **RabbitMQ**             | Event broker for booking notifications       | `5672 / 15672` |

---

## Technologies Used

- **Spring Boot 3**, **Spring Cloud**
- **Eureka Discovery**, **Config Server**
- **OpenFeign**, **Circuit Breaker (Resilience4J)**
- **MongoDB**, **RabbitMQ**
- **JWT Security**
- **Docker Compose**
- **JUnit + Mockito (95–100% coverage supported)**

---

## Features

### Authentication

User registration, login, JWT-based security.

### Flight Management

Flight search, admin inventory, seat updates.

### Booking

Booking tickets, cancelling, viewing ticket details, booking history.

### Notifications

RabbitMQ message publishing & consuming.

### Config-Driven Architecture

All configs dynamically loaded from a Git-based config server.

### Service Discovery

All services auto-register with Eureka.

### Fault Tolerance

Circuit Breaker protects against Flight Service downtime.
