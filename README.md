[![Microservices CI Pipeline](https://github.com/007RajatSingh/ecommerce-microservices/actions/workflows/backend-cicd.yml/badge.svg)](https://github.com/007RajatSingh/ecommerce-microservices/actions/workflows/backend-cicd.yml)

# E-Commerce Microservices Backend

A robust, highly scalable e-commerce backend architecture built entirely with Spring Boot Microservices. This project demonstrates enterprise-grade patterns including Event-Driven Architecture, decentralized data management, comprehensive fault tolerance, and centralized observability.

---

## Technical Stack

* **Core Framework:** Java 17, Spring Boot 3, Spring Cloud
* **Persistence:** PostgreSQL (Database-per-Service Pattern), Redis
* **Asynchronous Messaging:** Apache Kafka (Saga Pattern & Outbox Events)
* **Security & Routing:** Spring Cloud Gateway, JWT (JSON Web Tokens) with strict Admin/Client isolation
* **Resilience:** Resilience4j (Circuit Breakers, TimeLimiters, Fallbacks)
* **Observability:** Micrometer, Zipkin (Distributed Tracing), Grafana, Loki, Promtail (Centralized Logging)
* **Deployment:** Docker & Docker Compose

---

## System Architecture

The system is decomposed into highly specialized, isolated microservices. All external traffic routes through the API Gateway, which handles authentication and load balancing.

### Core Services
1. **API Gateway (Port 8080):** The single entry point. Handles intelligent routing, load balancing, and strict JWT validation via custom security filters. Includes fallback controllers for degraded services.
2. **Discovery Service (Port 8761):** Netflix Eureka server for dynamic service registry and discovery.
3. **Client Auth Service (Port 9090):** Manages standard customer registrations and issues client JWTs. Uses `auth_db`.
4. **Admin Auth Service (Port 9091):** Physically isolated authentication server for warehouse administrators. Uses distinct cryptographic secrets and `admin_db` to guarantee privilege separation.
5. **Product Service (Port 8081):** Manages the product catalog and pricing. Uses `product_db`.
6. **Order & Cart Service (Port 8083):** Handles shopping carts and checkout processes. Communicates synchronously with Product Service via OpenFeign and asynchronously via Kafka. Uses `order_db`.
7. **Inventory Service (Port 8082):** Manages stock levels. Subscribes to Kafka events to deduct inventory when orders are placed. Uses `inventory_db`.
8. **Notification Service (Port 8084):** Listens to the Kafka event stream to process and send asynchronous email notifications.
9. **Payment Service:** Processes simulated transactions asynchronously.

---

## Key Features

### 1. Advanced Security Isolation
The architecture enforces strict separation of concerns. Standard users and administrators are authenticated by completely different microservices (`auth-service` vs `admin-auth-service`), backed by different PostgreSQL databases, and validated against different cryptographic JWT secrets at the Gateway level.

### 2. Comprehensive Fault Tolerance
Designed to prevent cascading failures:
- **API Gateway Circuit Breakers:** Monitors downstream health. If a service (e.g., Product Service) fails, Resilience4j instantly opens the circuit and routes requests to a graceful Fallback Controller.
- **OpenFeign Fallbacks:** Synchronous internal HTTP calls are wrapped in circuit breakers to ensure a crashing dependency doesn't crash the caller.
- **Kafka Dead Letter Queues (DLQ):** Poisonous messages or unrecoverable processing errors are caught, retried with backoff, and ultimately safely stored in a DLT (Dead Letter Topic) to prevent queue blocking and data loss.

### 3. Centralized Observability
A unified pane of glass for monitoring system health:
- **Distributed Tracing:** Micrometer and Zipkin inject unique Trace IDs into every HTTP request and Kafka message, mapping the complete journey of a transaction across boundaries.
- **Centralized Logging:** Promtail dynamically scrapes `stdout` from all Docker containers and ships the logs to Grafana Loki.
- **Unified Dashboard:** Grafana automatically provisions data sources, allowing developers to query logs and click directly through to Zipkin trace graphs.

---

## Local Development & Deployment

### Prerequisites
* Docker and Docker Compose installed.
* Ports `8080`, `3000`, `3100`, `8761`, `9411`, `9090-9092`, and `8081-8084` available.

### One-Click Start
To build and deploy the entire cluster, simply run:

```bash
docker compose up -d --build
```

### Accessing Dashboards
- **API Gateway:** `http://localhost:8080`
- **Eureka Service Registry:** `http://localhost:8761`
- **Grafana (Logs & Traces):** `http://localhost:3000` (Credentials: admin/admin)
- **Zipkin (Raw Traces):** `http://localhost:9411`
