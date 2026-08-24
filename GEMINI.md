# [PROJECT CONTEXT]: {KickSync}

## 1. Tech Stack & Standards
- **Language/Framework**: Java 21 (Virtual Threads active), Spring Boot 3.5.5
- **Database**: MySQL 8.0, Redis (Redisson for Distributed Locks)
- **Naming Convention**: camelCase for methods/variables, PascalCase for classes.

## 2. Architectural Guardrails (STRICT)
- **Fetch Strategy**: All JPA associations MUST use `FetchType.LAZY`.
- **Concurrency Control**: Sensitive shared resources MUST use `@DistributedLock`. 
  - Implementation Reference: `src/main/java/be/kicksync_backend/common/annotation/DistributedLock.java`
- **Error Handling**: Use `CustomException(ErrorCode.XYZ)` only. No generic exceptions.

## 3. Directory Structure
- `src/main/java/be/kicksync_backend/feature/{domain}/`: Feature-based packaging.
- `tests/load/`: k6 load testing scripts.
- `conductor/`: Project planning and track management files.

## 4. Git & Tool Workflow
- Commit: Conventional Commits format. Focus on "Why" in the body.
- Verification: Run `./gradlew test` and linter before marking any task as [DONE].
