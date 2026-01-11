# KickSync - 대용량 트래픽 처리를 위한 한정판 신발 거래 플랫폼

> **"100만 건의 정산 데이터 처리 속도를 136배 단축하고, 동시성 제어를 통해 재고 오차 0%를 달성한 고성능 백엔드 서버"**

<br>

## 프로젝트 소개
**KickSync**는 KREAM, StockX와 같은 한정판 신발 중개 거래 플랫폼을 모티브로 한 대용량 트래픽 처리 프로젝트입니다.

단순한 기능 구현을 넘어, **대규모 데이터 처리 시 발생하는 성능 병목을 단계적으로 해결**하고, **분산 환경에서의 데이터 정합성**을 보장하는 견고한 아키텍처를 구축하는 데 주력했습니다. 이를 통해 시스템이 성장함에 따라 발생하는 기술적 부채를 해결하고 확장성 있는 구조로 고도화하는 과정을 경험했습니다.

### 핵심 목표
- **대용량 배치 최적화:** 100만 건 이상의 정산 데이터 처리 속도 대폭 개선 (**136배 단축**)
- **데이터 무결성:** 인기 상품 주문 폭주 상황에서 **재고 정합성 100% 보장**
- **고가용성 & 확장성:** 트래픽 증가와 서버 증설에 유연한 아키텍처 설계

<br>

## 기술 스택

| Category | Technology |
| :--- | :--- |
| **Language** | <img src="https://img.shields.io/badge/Java_17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white"> |
| **Framework** | <img src="https://img.shields.io/badge/Spring_Boot_3.x-6DB33F?style=for-the-badge&logo=spring&logoColor=white"> <img src="https://img.shields.io/badge/Spring_Batch-6DB33F?style=for-the-badge&logo=spring&logoColor=white"> <img src="https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white"> |
| **Database** | <img src="https://img.shields.io/badge/MySQL_8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white"> <img src="https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white"> |
| **ORM** | <img src="https://img.shields.io/badge/Hibernate-59666C?style=for-the-badge&logo=hibernate&logoColor=white"> <img src="https://img.shields.io/badge/QueryDSL-007ACC?style=for-the-badge&logo=java&logoColor=white"> |
| **Infra & Testing** | <img src="https://img.shields.io/badge/Oracle_Cloud-F80000?style=for-the-badge&logo=oracle&logoColor=white"> <img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white"> <img src="https://img.shields.io/badge/Nginx-009639?style=for-the-badge&logo=nginx&logoColor=white"> <img src="https://img.shields.io/badge/GitHub_Actions-2088FF?style=for-the-badge&logo=github-actions&logoColor=white"> <br> <img src="https://img.shields.io/badge/JUnit5-25A162?style=for-the-badge&logo=junit5&logoColor=white"> <img src="https://img.shields.io/badge/nGrinder-FFA500?style=for-the-badge&logo=java&logoColor=white"> <img src="https://img.shields.io/badge/Scouter-00C7B7?style=for-the-badge&logo=scouter&logoColor=white"> |

<br>

## 시스템 아키텍처
<img width="1071" height="591" alt="KickSync System Architecture" src="https://github.com/user-attachments/assets/318e28a2-1d6c-498a-b7e0-7826d9182e79" />

## 배치 프로세스 아키텍처
<img width="895" height="561" alt="Batch Process Architecture" src="https://github.com/user-attachments/assets/7e991801-31e0-41a3-aa0e-928188e5eba3" />

<br>

## 성능 고도화 과정

서비스 초기 단계부터 데이터 규모 증가에 따라 발생한 병목을 **'가설-검증-개선'** 의 프로세스로 해결하며 아키텍처를 진화시켰습니다.

### Phase 1. 분산 환경의 안정성 및 기초 성능 확보
초기 Spring Scheduler 기반의 정산 시스템 운영 중 발견된 분산 환경의 구조적 문제와 네트워크 비효율을 개선했습니다.

- **Issue 1: 분산 환경에서의 스케줄러 중복 실행**
    - **문제:** 서버를 Scale-out 하자, 동일한 스케줄러가 각 인스턴스에서 동시에 실행되어 정산 데이터가 중복 생성되는 무결성 위협 발생.
    - **해결:** **ShedLock**을 도입하여 분산 환경의 여러 인스턴스 중 단 하나만 실행되도록 락을 걸어 데이터 정합성 100% 보장.
    
- **Issue 2: 대량 Insert 시 통신 오버헤드**
    - **문제:** 일일 데이터 10만 건 처리 시, JPA의 단건 `save()` 방식은 DB와 잦은 통신을 유발하여 약 **20초(20,259ms)** 소요.
    - **해결:** **JDBC Bulk Insert**를 적용하여 Insert 쿼리를 하나로 묶어 네트워크 왕복 비용을 최소화.
    - **성과:** 처리 시간 **676ms**로 단축 (**약 96% 성능 개선**).

<br>

### Phase 2. 대용량 처리를 위한 아키텍처 재설계 (Spring Batch 도입)

데이터가 100만 건 이상으로 증가함에 따라, 기존 방식의 한계를 넘기 위해 **Spring Batch**로 마이그레이션했습니다. 단순 마이그레이션에 그치지 않고, **2시간 23분**이 걸리던 작업을 **1분 2초**로 단축시키는 단계별 최적화를 수행했습니다.

#### 성능 개선 요약

| 최적화 단계 | 실행 시간 (100만 건) | 이전 대비 향상 | 메모리 사용량 | 핵심 성과 및 교훈 |
| :--- | :--- | :--- | :--- | :--- |
| **0. `ListItemReader`** | 3분 8초 | - | **+1,175 MB** | OOM 위험으로 운영 불가 (메모리 부족) |
| **1. `JpaPagingItemReader`** | **2시간 23분** | - | **-17 MB** | 메모리 안정성 확보, But **I/O 병목 발생** |
| **2. + 커버링 인덱스** | 19분 34초 | **7.3배** | +17 MB | `filesort` 제거, **I/O 최적화 기반 마련** |
| **3. + 멀티 스레드 스텝** | 17분 37초 | **1.1배** | +27 MB | CPU 병렬 처리 한계 확인 → **I/O가 핵심 병목임을 증명** |
| **4. + 파티셔닝** | **1분 2초** | **18.9배** | +115 MB | **I/O 병렬 처리로 병목 해결**, 수평적 확장성 확보 |

<br>

#### 상세 트러블슈팅 과정

**Step 0 ~ 1. 메모리 안정성 확보와 새로운 병목 (OOM 해결)**
- **문제:** 초기 `ListItemReader`는 모든 데이터를 메모리에 로드하여 OOM 위험이 컸습니다.
- **해결:** `JpaPagingItemReader`를 도입하여 Chunk 단위로 데이터를 처리, 메모리 사용량을 1,175MB → **115MB (약 90% 절감)** 로 안정화했습니다.
- **새로운 문제:** 페이징 쿼리의 Offset이 커질수록 DB I/O가 급증하여 실행 시간이 **2시간 23분**으로 대폭 증가했습니다.

**Step 2. 인덱스 튜닝을 통한 I/O 최적화**
- **분석:** `EXPLAIN` 분석 결과 `Using filesort`가 발생하여 디스크 정렬 부하가 심함을 확인했습니다.
- **해결:** 쿼리에 사용되는 컬럼(`status`, `partner_id`, `payment_date`, `payment_amount`)으로 **커버링 인덱스**를 생성했습니다.
- **결과:** 디스크 I/O를 최소화하여 실행 시간을 **19분대**로 단축시켰습니다.

**Step 3. 멀티 스레드 도입과 병목 지점 검증**
- **가설:** "단순 연산량 문제라면 CPU 병렬 처리가 효과적일 것이다."
- **실험:** `ThreadPoolTaskExecutor`를 적용하여 멀티 스레드 처리를 시도했으나 성능 향상은 1.1배에 그쳤습니다.
- **결론:** 이를 통해 CPU가 아닌 **Single Thread의 DB 조회(I/O)** 가 근본적인 병목임을 증명했습니다.

**Step 4. 파티셔닝 아키텍처 도입 (최종 솔루션)**
- **해결:** I/O 자체를 병렬로 처리하기 위해 **Spring Batch Partitioning**을 도입했습니다. `Manager Step`이 데이터 범위를 분할하고, 다수의 `Worker Step`이 병렬로 DB에 접근하는 구조로 재설계했습니다.
- **결과:** 최종 실행 시간 **1분 2초** 달성 (**속도 136배 향상**)

**[파티셔닝 아키텍처 구조도]**
<img width="2432" height="1760" alt="Partitioning Architecture" src="https://github.com/user-attachments/assets/0d74dd20-e17a-4dcd-ab02-d39a4c51d9d6" />

<br>

## 동시성 제어 및 캐싱 전략

### 1. Redisson 분산 락을 활용한 재고 정합성 확보 (오차 0건)
**상황:** 인기 상품 주문 시 동시에 다수의 요청이 들어올 경우, Race Condition으로 인해 재고가 음수가 되거나 초과 주문되는 현상 발생.

**해결:**
- **Redis 분산 락 (Redisson):** DB 락(Pessimistic Lock) 대신 Redis를 사용하여 DB 커넥션 점유 시간을 최소화하고 부하를 분산.
- **Pub/Sub 방식:** Spin Lock 방식의 Lettuce 대신, 부하가 적은 Pub/Sub 방식의 Redisson을 채택하여 Redis 서버의 CPU 사용률 최적화.
- **Facade 패턴:** 비즈니스 로직과 락 처리 로직을 분리하여 코드의 유지보수성 및 가독성 향상.
**결과:** nGrinder를 이용한 대규모 동시성 테스트 결과, **재고 오차 0건** 달성.

### 2. Redis 캐싱을 통한 조회 성능 236% 향상 (nGrinder)
**배경 및 원인:**
- **I/O 병목:** 전체 상품 목록 조회 API는 호출 빈도가 매우 높으나 데이터 변경은 드물어, 매번 DB를 조회하는 것은 불필요한 I/O 자원 낭비 및 응답 지연을 초래.
- **DB 부하:** 트래픽 급증 시 반복적인 단순 조회 쿼리로 인해 DB 커넥션 풀 고갈 위험 확인.

**해결 전략:**
1.  **Look-aside 캐싱 전략 (`@Cacheable`):**
    -   API 호출 시 Redis를 우선 확인하여(Cache Hit) DB 접근을 차단.
    -   데이터가 없을 경우에만 DB를 조회하고 결과를 캐시에 적재.
2.  **데이터 정합성 보장 (`@CacheEvict`):**
    -   관리자에 의한 상품 추가/수정/삭제 발생 시, 관련 캐시 그룹(`products`) 전체를 즉시 무효화하여 **오래된 데이터 문제 원천 차단.**

**성능 개선 결과 (페이징 조회 기준):**
nGrinder 부하 테스트 결과, 캐시 적용 후 **TPS가 약 3.3배 증가**하고 **응답 시간은 73% 단축**되었습니다.

| 지표 (Metrics) | 캐시 적용 전 | 캐시 적용 후 | 개선 효과 |
| :--- | :--- | :--- | :--- |
| **TPS (평균 처리량)** | 1,736.7 | **5,831.9** | **약 236% 향상** |
| **Peak TPS** | 2,194.5 | **6,698.0** | **205% 향상** |
| **Mean Test Time** | 26.70ms | **6.99ms** | **73.8% 단축** |
| **Total Executed Tests** | 515,159 | **1,728,652** | **235% 처리량 증가** |

<br>

<details>
<summary><b>nGrinder 부하 테스트 결과 스크린샷 (펼쳐보기)</b></summary>
<div markdown="1">

#### 1. 전체 상품 조회 (1000건)

[캐시 미적용] <img width="2048" height="607" alt="image" src="https://github.com/user-attachments/assets/a8a60f55-ded3-463d-a12f-52f7bea0e1bd" />
[캐시 적용] <img width="2048" height="609" alt="image" src="https://github.com/user-attachments/assets/d146f350-f8ba-4463-8479-955a9f7e6e08" />

#### 2. 페이징 조회 (40건) - *Main Result*


[캐시 미적용] <img width="2048" height="613" alt="image" src="https://github.com/user-attachments/assets/93da321c-1319-4b59-a299-cbcc1e1f3dd7" />
[캐시 적용] <img width="2048" height="613" alt="image" src="https://github.com/user-attachments/assets/d667326a-7e62-499d-bcdb-b29fc57f3255" />

</div>
</details>

<br>

## ERD
<img width="2300" height="1272" alt="ERD" src="https://github.com/user-attachments/assets/5dace7c6-d2d5-4f9c-92ac-2b5bbc95f64e" />
https://www.erdcloud.com/d/B5xBxsPqkP4uwSPt4
<br>
