# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Development Commands

### Build and Run
- `./gradlew build` - Build the project
- `./gradlew bootRun` - Run the Spring Boot application
- `./gradlew test` - Run all tests
- `./gradlew test --tests ClassName` - Run specific test class
- `./gradlew test --tests ClassName.methodName` - Run specific test method

### Development Tools
- Uses Gradle Wrapper (gradlew/gradlew.bat) - always use wrapper instead of global gradle
- Hot reload enabled via Spring Boot DevTools in development
- H2 in-memory database for tests, MySQL for production

## Architecture Overview

### Core Technologies
- **Spring Boot 3.5.5** with Java 21
- **Spring Security** with JWT authentication
- **Spring Data JPA** with MySQL (prod) / H2 (test)
- **Spring Batch** for background processing
- **Spring WebSocket** for real-time notifications
- **ShedLock** for distributed task scheduling
- **Payment Integration**: Iamport (Portone) and Toss payments

### Domain-Driven Structure
The codebase follows a feature-based package structure under `src/main/java/be/kicksync_backend/`:

#### Common Infrastructure (`common/`)
- `config/` - Security, JWT, WebSocket, Batch configurations
- `dto/` - Shared response objects (ApiResponse, ErrorResponse)
- `entity/` - BaseTimeEntity for audit fields
- `exception/` - Global exception handling and custom errors
- `security/` - JWT utilities and authentication
- `util/` - Shared utilities

#### Feature Modules (`feature/`)
Each feature follows consistent structure: `controller/`, `dto/`, `entity/`, `repository/`, `service/`

**Core Business Features:**
- **`user/`** - User management, authentication, profiles
- **`product/`** - Product catalog, drop events (limited releases)
- **`order/`** - Order lifecycle management with state transitions
- **`payment/`** - Payment processing with external gateways
- **`settlement/`** - Financial settlements between parties
- **`notification/`** - WebSocket-based real-time notifications

**Supporting Features:**
- **`token/`** - JWT refresh token management
- **`admin/`** - Administrative functions
- **`batch/`** - Background job processing

### Key Domain Relationships
- **User** ↔ **Order** (one-to-many): Users can have multiple orders
- **Order** ↔ **Payment** (one-to-one): Each order has exactly one payment
- **Order** ↔ **Settlement** (one-to-one): Completed orders generate settlements
- **Order** ↔ **OrderItem** (one-to-many): Orders contain multiple products
- **Product** ↔ **DropEvent** (many-to-one): Products can be part of timed releases

### Order State Machine
Orders follow strict state transitions:
`PENDING_PAYMENT` → `PREPARING` → `SHIPPED` → `DELIVERED` → `SETTLED`
- Branch states: `PAYMENT_FAILED`, `CANCELLED`
- State validation enforced in entity methods

### Payment Integration
- Dual payment provider support (Iamport + Toss)
- Comprehensive transaction tracking with PG details
- Automatic settlement processing via Spring Batch

### Security Architecture
- JWT-based stateless authentication
- Separate access/refresh token strategy
- WebSocket authentication via JWT
- Environment-based configuration (no hardcoded secrets)

## Configuration Notes
- Production config uses environment variables for sensitive data
- Test config includes hardcoded test values for payment providers
- Database DDL auto-managed (update in prod, create-drop in tests)
- Spring Batch and Scheduling enabled at application level

## Environment Setup
- When a task is completed or the session stops, always run the following command: `afplay /System/Library/Sounds/Glass.aiff`