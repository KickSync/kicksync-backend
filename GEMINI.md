# KickSync Backend: Project Guide

## Project Overview
**KickSync** is a high-performance e-commerce backend specializing in limited-edition transactions. It handles high-concurrency traffic, complex settlement processing, and strict inventory integrity using distributed locks.

## Tech Stack
- **Language:** Java 21 (Virtual Threads enabled)
- **Framework:** Spring Boot 3.5.5, Spring Security (JWT)
- **Database:** MySQL 8.0, Redis (Cache & Redisson Distributed Locks)
- **Batch:** Spring Batch (Partitioned & Chunk-oriented)
- **Real-time:** Spring WebSocket with Stomp
- **Documentation:** Swagger/OpenAPI 3.0

## Core Architecture
### 1. Concurrency Control
- **Redisson Distributed Locks**: Used via custom AOP (`@DistributedLock`) to ensure inventory integrity.
- **Transaction Alignment**: Locks wrap database transactions to prevent race conditions during commit.

### 2. Settlement & Batch Processing
- **High Efficiency**: Optimized to process 1M records in ~1.1s.
- **Partitioning**: Multi-threaded execution across partitioned datasets for maximum throughput.

### 3. Caching Strategy
- **Look-aside Strategy**: Redis caching with 10-minute TTL for high-traffic read APIs (e.g., product drops).

## Development Standards
### Package Structure (Feature-Based)
Follow the pattern: `be.kicksync_backend.feature.{domain}.{controller,service,repository,entity,dto}`
- `common/`: Shared infrastructure (config, exception, security, util).
- `feature/`: Cohesive business modules (user, order, payment, settlement, etc.).

### Order State Machine
Orders must follow strict transitions:
`PENDING_PAYMENT` → `PREPARING` → `SHIPPED` → `DELIVERED` → `SETTLED`
- Failure/Cancel states: `PAYMENT_FAILED`, `CANCELLED`
- Validation is enforced at the entity/service level.

### API Documentation
- Use Swagger annotations for all REST controllers.
- Ensure DTOs are well-documented for frontend integration.

## 🛡️ Consistency Guardrail (Architectural Persistence)
This section defines immutable rules for AI-driven modifications to ensure long-term stability and performance.

### 1. JPA & Entity Integrity
- **Strict LAZY Loading**: All `@ManyToOne`, `@OneToOne`, and `@OneToMany` associations MUST use `FetchType.LAZY`. EAGER loading is strictly forbidden to prevent N+1 and performance degradation.
- **Loose Coupling (Cross-Domain)**: Between different feature domains (e.g., Order and Settlement), prefer referencing by ID (`Long partnerId`) rather than direct JPA Entity associations. This prevents circular dependencies and complex object graphs.
- **BaseTimeEntity**: Every entity MUST extend `BaseTimeEntity` for auditing.

### 2. Concurrency & Logic
- **Distributed Lock Over Transactions**: Methods modifying sensitive shared resources (Inventory, Wallet) MUST be annotated with `@DistributedLock`. The lock MUST be acquired before the database transaction starts.
- **Domain-Driven Transitions**: Entity state changes MUST be encapsulated within the Entity (e.g., `order.ship()`) with proper state validation, rather than direct status setter calls in the Service layer.

### 3. Error Handling
- **Custom Exceptions**: NEVER use generic exceptions. Use `CustomException(ErrorCode.XYZ)` to ensure consistent API responses.

## Standard Commands
### Build & Run
- `./gradlew build`: Full project build.
- `./gradlew bootRun --args='--spring.profiles.active=local'`: Run locally.
- `./gradlew test`: Execute all tests.

### Testing
- **Unit/Integration**: Use Spring Boot Test.
- **Load Testing**: Scripts located in `tests/load/` using k6.

## Environment Setup
1. `cp src/main/resources/application-local.yml.example src/main/resources/application-local.yml`
2. Update local MySQL/Redis credentials and JWT secrets.
