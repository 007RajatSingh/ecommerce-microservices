# 🛒 E-Commerce Microservices Backend

A robust, scalable e-commerce backend built with **Spring Boot Microservices**.
It features **Event-Driven Architecture** (Kafka), **Edge Security** (JWT + API Gateway), and **Resilience patterns** (Circuit Breakers).

## 🚀 Tech Stack
* **Core:** Java 17, Spring Boot 3, Spring Cloud
* **Database:** PostgreSQL (Per-service DB pattern), Redis (Caching)
* **Messaging:** Apache Kafka (Async Notifications)
* **Security:** Keycloak / JWT Authentication, OAuth2
* **Resilience:** Resilience4j (Circuit Breakers)
* **Tracing:** Micrometer & Zipkin (Distributed Tracing)
* **Containerization:** Docker & Docker Compose

## 🏗 Architecture
1.  **API Gateway (8080):** The entry point. Handles routing & JWT validation.
2.  **Discovery Service (8761):** Service Registry (Eureka).
3.  **Auth Service (9090):** Issues & Validates JWT Tokens.
4.  **Product Service (8081):** Manages inventory catalog.
5.  **Order Service (8083):** Places orders & triggers Kafka events.
6.  **Inventory Service (8082):** Checks stock availability.
7.  **Notification Service (8084):** Listens to Kafka & "sends" emails.

## 🛠 How to Run

### Prerequisites
* Docker & Docker Compose installed.
* Java 17 (optional, only for local dev).

### 1-Click Start
Run the entire system with Docker Compose:
```bash
docker-compose up -d --build