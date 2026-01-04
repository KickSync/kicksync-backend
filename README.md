
# KickSync - 대용량 트래픽 처리를 위한 한정판 신발 거래 플랫폼

> **"100만 건의 정산 데이터 처리 속도를 136배 단축하고, 동시성 제어를 통해 재고 오차 0%를 달성한 고성능 백엔드 서버"**

<br>

## 프로젝트 소개
**KickSync**는 KREAM, StockX와 같은 한정판 신발 중개 거래 플랫폼을 모티브로 한 대용량 트래픽 처리 프로젝트입니다.

단순한 기능 구현을 넘어, **대규모 데이터 처리 시 발생하는 성능 병목을 단계적으로 해결**하고, **분산 환경에서의 데이터 정합성**을 보장하는 견고한 아키텍처를 구축하는 데 주력했습니다. 이를 통해 시스템이 성장함에 따라 발생하는 기술적 부채를 해결하고 확장성 있는 구조로 고도화하는 과정을 경험했습니다.

### 핵심 목표
- **대용량 배치 최적화:** 100만 건 이상의 정산 데이터 처리 속도 **99% 개선 (136배 단축)**
- **데이터 무결성:** 인기 상품 주문 폭주 상황에서 **재고 정합성 100% 보장**
- **고가용성 & 확장성:** 트래픽 증가와 서버 증설에 유연한 아키텍처 설계

<br>

## 기술 스택

| Category | Technology |
| --- | --- |
| **Language** | Java 17 |
| **Framework** | Spring Boot 3.x, Spring Batch, Spring Security |
| **Database** | MySQL 8.0, Redis (Cache, Lock, Pub/Sub) |
| **ORM** | JPA (Hibernate), QueryDSL |
| **DevOps** | AWS (EC2, RDS), Docker, GitHub Actions |
| **Testing** | JUnit5, nGrinder, Scouter |

<br>

## 시스템 아키텍처
<img width="1071" height="591" alt="image" src="https://github.com/user-attachments/assets/318e28a2-1d6c-498a-b7e0-7826d9182e79" />


<br>

## 성능 고도화 과정

서비스 초기 단계부터 데이터 규모 증가에 따라 발생한 병목을 **'가설-검증-개선'** 의 프로세스로 해결하며 아키텍처를 진화시켰습니다.

### Phase 1. 분산 환경의 안정성 및 기초 성능 확보
초기 Spring Scheduler 기반의 정산 시스템 운영 중 발견된 분산 환경의 구조적 문제와 네트워크 비효율을 개선했습니다.

- **Issue 1: 분산 환경에서의 스케줄러 중복 실행**
    - **문제:** 서버를 Scale-out 하자, 동일한 스케줄러가 각 인스턴스에서 동시에 실행되어 정산 데이터가 중복 생성되는 무결성 위협 발생.
    - **해결:** **ShedLock**을 도입하여 분산 환경의 여러 인스턴스 중 단 하나만 실행되도록 락(Lock)을 걸어 데이터 정합성 100% 보장.
    
- **Issue 2: 대량 Insert 시 통신 오버헤드**
    - **문제:** 일일 데이터 10만 건 처리 시, JPA의 단건 `save()` 방식은 DB와 잦은 통신을 유발하여 약 **20초(20,259ms)** 소요.
    - **해결:** **JDBC Bulk Insert**를 적용하여 Insert 쿼리를 하나로 묶어 네트워크 왕복 비용(Round Trip)을 최소화.
    - **성과:** 처리 시간 **676ms**로 단축 (**약 96% 성능 개선**).

<br>

### Phase 2. 대용량 처리를 위한 아키텍처 재설계 (Spring Batch 도입)
데이터가 100만 건 이상으로 증가함에 따라, 기존 방식의 한계를 넘기 위해 **Spring Batch**로 마이그레이션하고 심화된 성능 병목을 해결했습니다.

- **Challenge: 마이그레이션 후에도 해결되지 않은 병목 (I/O vs CPU)**
    - Spring Batch 도입 후에도 100만 건 처리에 **2시간 23분**이 소요되었으며, 메모리 이슈가 발생했습니다.

**[Troubleshooting Log]**
1.  **메모리 최적화:** `JpaPagingItemReader`를 도입하여 데이터를 페이지 단위로 조회, 메모리 점유율을 **1,175MB → 115MB (90% 절감)** 로 안정화.
2.  **가설 검증 (CPU vs I/O):** "단순 연산량 문제"로 가정하고 멀티 스레드를 적용했으나 성능 향상 미미함 → 모니터링을 통해 **DB I/O 대기가 진짜 병목임을 확인**.
3.  **파티셔닝 아키텍처 도입:** DB I/O 병목 해소를 위해 **Spring Batch Partitioning**을 도입, 데이터 범위를 나눠 다수의 워커가 병렬로 처리하도록 구조 변경.

**최종 성과 비교**

| 단계 | 처리 방식 | 소요 시간 | 비고 |
| :---: | :--- | :---: | :--- |
| **Before** | Spring Scheduler (단일 처리) | 2시간 23분 | 메모리 1.1GB 점유 |
| **After** | **Spring Batch Partitioning** | **1분 2초**  | **속도 136배 향상** / 메모리 115MB |

<br>

## 동시성 제어 및 캐싱 전략

### 1. Redisson 분산 락을 활용한 재고 정합성 확보 (오차 0건)
**상황:** 인기 상품 주문 시 동시에 다수의 요청이 들어올 경우, Race Condition으로 인해 재고가 음수가 되거나 초과 주문되는 현상 발생.

**해결:**
- **Redis 분산 락 (Redisson):** DB 락(Pessimistic Lock) 대신 Redis를 사용하여 DB 커넥션 점유 시간을 최소화하고 부하를 분산.
- **Pub/Sub 방식:** Spin Lock 방식의 Lettuce 대신, 부하가 적은 Pub/Sub 방식의 Redisson을 채택하여 Redis 서버의 CPU 사용률 최적화.
- **Facade 패턴:** 비즈니스 로직과 락 처리 로직을 분리하여 코드의 유지보수성 및 가독성 향상.
**결과:** nGrinder를 이용한 대규모 동시성 테스트 결과, **재고 오차 0건** 달성.

### 2. Redis Caching을 통한 조회 성능 236% 향상
**상황:** 메인 페이지의 상품 조회 API 호출 빈도가 높아 DB 부하가 심화됨.

**해결:**
- **Look-aside 전략:** 자주 조회되는 데이터는 Redis에 캐싱하여 DB 접근 횟수 최소화.
- **Cache Warming:** 주요 상품 데이터를 미리 캐싱하여 배포 직후 발생하는 초기 Latency 방지.
**결과:** API 처리량(TPS) **236% 향상**.

<br>

## ERD
<img width="2300" height="1272" alt="image" src="https://github.com/user-attachments/assets/dea6bddd-e37b-4663-b63b-7212dc0a4e02" />
https://www.erdcloud.com/d/B5xBxsPqkP4uwSPt4
<br>
