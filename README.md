# KickSync - 대용량 트래픽 처리를 위한 한정판 신발 거래 플랫폼

> **"100만 건의 정산 데이터 처리 속도를 136배 단축하고 재고 오차 0%를 달성한 고성능 백엔드 서버"**

<br>

## 프로젝트 소개
**KickSync**는 KREAM, StockX를 모티브로 한 대용량 트래픽 처리 프로젝트입니다.

단순 기능 구현을 넘어 대규모 트래픽 환경의 성능 병목 해결과 데이터 정합성 보장에 주력했습니다. 시스템 확장에 따른 기술적 부채를 해결하며 유연한 확장성을 갖춘 견고한 아키텍처로 고도화했습니다.

### 핵심 목표
- **대용량 배치 최적화:** 100만 건 정산 데이터 처리 속도 136배 단축 (**2시간 23분 → 1분 2초**)
- **데이터 무결성:** 고부하 상황 내 재고 정합성 **100% 보장**
- **고가용성 및 확장성:** 트래픽 증가와 인프라 확장에 유연한 **분산 아키텍처 설계**

<br>

## 기술 스택

| Category | Technology |
| :--- | :--- |
| **Language** | <img src="https://img.shields.io/badge/Java_21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white"> |
| **Framework** | <img src="https://img.shields.io/badge/Spring_Boot_3.5.5-6DB33F?style=for-the-badge&logo=spring&logoColor=white"> <img src="https://img.shields.io/badge/Spring_Batch-6DB33F?style=for-the-badge&logo=spring&logoColor=white"> <img src="https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white"> |
| **Database** | <img src="https://img.shields.io/badge/MySQL_8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white"> <img src="https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white"> |
| **ORM** | <img src="https://img.shields.io/badge/Hibernate-59666C?style=for-the-badge&logo=hibernate&logoColor=white"> <img src="https://img.shields.io/badge/QueryDSL-007ACC?style=for-the-badge&logo=java&logoColor=white"> |
| **Infra & Testing** | <img src="https://img.shields.io/badge/Oracle_Cloud-F80000?style=for-the-badge&logo=oracle&logoColor=white"> <img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white"> <img src="https://img.shields.io/badge/Nginx-009639?style=for-the-badge&logo=nginx&logoColor=white"> <img src="https://img.shields.io/badge/GitHub_Actions-2088FF?style=for-the-badge&logo=github-actions&logoColor=white"> <br> <img src="https://img.shields.io/badge/JUnit5-25A162?style=for-the-badge&logo=junit5&logoColor=white"> <img src="https://img.shields.io/badge/nGrinder-FFA500?style=for-the-badge&logo=java&logoColor=white"> <img src="https://img.shields.io/badge/Scouter-00C7B7?style=for-the-badge&logo=scouter&logoColor=white"> |

<br>

## 시스템 아키텍처
<img width="1071" height="591" alt="KickSync System Architecture" src="https://github.com/user-attachments/assets/318e28a2-1d6c-498a-b7e0-7826d9182e79" />

<br><br>

## 배치 프로세스 아키텍처
<img width="895" height="561" alt="Batch Process Architecture" src="https://github.com/user-attachments/assets/7e991801-31e0-41a3-aa0e-928188e5eba3" />

<br><br><br>

## 성능 고도화 과정 & 트러블 슈팅

데이터 규모 증가에 따른 병목을 **'가설-검증-개선'** 프로세스로 해결하며 아키텍처를 진화시켰습니다.


<details>
<summary><h3> Phase 1. 분산 환경의 안정성 및 기초 성능 확보 </h3></summary>
<div markdown="1">

Spring Scheduler 기반 시스템의 구조적 한계와 네트워크 비효율을 개선했습니다.

**1. 분산 환경에서의 스케줄러 중복 실행 방지**
- **[문제]** Scale-out 환경에서 동일 스케줄러가 중복 실행되어 정산 데이터 무결성 위협 발생
- **[해결]** **ShedLock**을 도입하여 분산된 인스턴스 중 단 하나의 노드만 작업을 수행하도록 제어
- **[결과]** 다중 서버 환경에서도 데이터 정합성 100% 보장

**2. 대량 Insert 시 통신 오버헤드 제거**
- **[문제]** JPA `save()` 방식 사용 시 10만 건 처리에 약 20초 소요 (잦은 네트워크 I/O 발생)
- **[해결]** **JDBC Bulk Insert**를 적용하여 쿼리를 단일 패킷으로 묶어 전송
- **[결과]** 처리 시간 20,259ms → **676ms (약 96% 성능 개선)**

</div>
</details>

<br>

<details>
<summary><h3> Phase 2. 대용량 처리를 위한 아키텍처 재설계 (Spring Batch 도입) </h3></summary>
<div markdown="1">

데이터 100만 건 증가에 대응하기 위해 **Spring Batch** 마이그레이션을 진행했습니다. 단계별 최적화를 통해 수행 시간을 **2시간 23분**에서 **1분 2초**로 단축했습니다.

<br>

### 성능 개선 요약

| 최적화 단계 | 실행 시간 (100만 건) | 이전 대비 향상 | 메모리 사용량 | 핵심 성과 및 교훈 |
| :--- | :--- | :--- | :--- | :--- |
| **0. `ListItemReader`** | 3분 8초 | - | **+1,175 MB** | OOM 위험으로 운영 불가 |
| **1. `JpaPagingItemReader`** | **2시간 23분** | - | **-17 MB** | 메모리 안정성 확보하였으나 **I/O 병목 발생** |
| **2. + 커버링 인덱스** | 19분 34초 | **7.3배** | +17 MB | `filesort` 제거 및 **I/O 최적화 기반 마련** |
| **3. + 멀티 스레드 스텝** | 17분 37초 | **1.1배** | +27 MB | CPU 병렬 처리 한계 확인 → **I/O가 핵심 병목임을 증명** |
| **4. + 파티셔닝** | **1분 2초** | **18.9배** | +115 MB | **I/O 병렬 처리로 병목 해결**, 수평적 확장성 확보 |

<br>

### [파티셔닝 아키텍처 구조도]
<img width="2432" height="1760" alt="Partitioning Architecture" src="https://github.com/user-attachments/assets/0d74dd20-e17a-4dcd-ab02-d39a4c51d9d6" />

<br><br>

### 상세 트러블슈팅 과정

**Step 0 ~ 1. 메모리 안정성 확보와 새로운 병목 (OOM 해결)**
- **[문제]** `ListItemReader`의 전체 데이터 로딩으로 OOM 위험 발생
- **[해결]** `JpaPagingItemReader` 도입 및 Chunk 단위 처리로 메모리 사용량 **115MB (약 90% 절감)** 안정화
- **[현상]** 페이징 쿼리의 Offset 증가로 DB I/O가 급증하여 실행 시간 **2시간 23분**으로 지연

**Step 2. 인덱스 튜닝을 통한 I/O 최적화**
- **[분석]** `EXPLAIN` 분석 결과 `Using filesort` 발생 확인 (디스크 정렬 부하)
- **[해결]** 주요 컬럼(`status`, `partner_id` 등) 대상 **커버링 인덱스** 생성
- **[결과]** 디스크 I/O 최소화로 실행 시간 **19분대** 단축

**Step 3. 멀티 스레드 도입과 병목 지점 검증**
- **[가설]** 단순 연산량 문제라면 CPU 병렬 처리가 효과적일 것
- **[실험]** `ThreadPoolTaskExecutor` 적용했으나 성능 향상 1.1배 수준에 그침
- **[결론]** CPU가 아닌 **Single Thread의 I/O 대기**가 근본적인 병목임을 규명

**Step 4. 파티셔닝 아키텍처 도입 (최종 솔루션)**
- **[해결]** **Spring Batch Partitioning** 도입. `Manager Step`이 범위를 분할하고 다수의 `Worker Step`이 병렬로 DB 접근
- **[결과]** 최종 실행 시간 **1분 2초** 달성 (**속도 136배 향상**)

</div>
</details>

<br>

<details>
<summary><h3> Phase 3. 동시성 제어 및 캐싱 전략 </h3></summary>
<div markdown="1">

### 1. Redisson 분산 락을 활용한 재고 정합성 확보

**상황 및 분석**
- 인기 상품 주문 시 Race Condition으로 재고 초과 차감(음수 재고) 현상 발생
- **DB Lock 한계:** 비관적 락 사용 시 커넥션 점유 시간 증가로 전체 장애 위험 존재
- **Redisson 채택:** 락 관리 책임을 Redis로 분리하고 **Pub/Sub 방식**을 활용하여 Redis 서버 부하 최소화

**구현 (AOP 기반 트랜잭션 분리)**
- **[문제]** `@Transactional` 커밋 전 락 해제로 동시성 이슈 재발
- **[해결]** Custom AOP(`@DistributedLock`) 구현으로 **'락 획득 → 트랜잭션 수행 → 락 해제'** 순서 강제

**결과**
- nGrinder 대규모 동시성 테스트 결과 **재고 오차 0건 달성**

<br>

### [nGrinder 테스트 환경]

<img width="700" height="800" alt="image" src="https://github.com/user-attachments/assets/13bb98f2-ee53-4533-b184-c9e1f136ef4a" />

<br><br>

### [분산 락 동작 로그]

```log
2026-01-13T18:07:57.129+09:00  INFO 6033 --- [kicksync_backend] [io-8080-exec-26] b.k.common.aop.DistributedLockAop        : [Redisson Lock] 락 획득 성공: MultiLock([LOCK:product:1]) (User=178)
2026-01-13T18:08:02.489+09:00  INFO 6033 --- [kicksync_backend] [io-8080-exec-26] b.k.feature.order.service.OrderService   : [CONCURRENCY_TEST] 재고 차감: User=178, ProductId=1, 남은 재고=9, 요청 수량=1
2026-01-13T18:08:02.752+09:00  INFO 6033 --- [kicksync_backend] [io-8080-exec-26] b.k.common.aop.DistributedLockAop        : [Redisson Lock] 락 해제 완료: MultiLock([LOCK:product:1]) (User=178)

2026-01-13T18:08:05.699+09:00  INFO 6033 --- [kicksync_backend] [io-8080-exec-17] b.k.feature.order.service.OrderService   : [CONCURRENCY_TEST] 재고 차감: User=189, ProductId=1, 남은 재고=8, 요청 수량=1
2026-01-13T18:08:05.796+09:00  INFO 6033 --- [kicksync_backend] [io-8080-exec-59] b.k.feature.order.service.OrderService   : [CONCURRENCY_TEST] 재고 차감: User=13, ProductId=1, 남은 재고=7, 요청 수량=1
....
2026-01-13T18:08:06.057+09:00  INFO 6033 --- [kicksync_backend] [o-8080-exec-125] b.k.feature.order.service.OrderService   : [CONCURRENCY_TEST] 재고 차감: User=76, ProductId=1, 남은 재고=1, 요청 수량=1
2026-01-13T18:08:06.066+09:00  INFO 6033 --- [kicksync_backend] [io-8080-exec-99] b.k.feature.order.service.OrderService   : [CONCURRENCY_TEST] 재고 차감: User=36, ProductId=1, 남은 재고=0, 요청 수량=1
2026-01-13T18:08:06.073+09:00  INFO 6033 --- [kicksync_backend] [io-8080-exec-99] b.k.common.aop.DistributedLockAop        : [Redisson Lock] 락 해제 완료: MultiLock([LOCK:product:1]) (User=36)


2026-01-13T18:08:06.080+09:00  INFO 6033 --- [kicksync_backend] [o-8080-exec-131] b.k.common.aop.DistributedLockAop        : [Redisson Lock] 락 획득 성공: MultiLock([LOCK:product:1]) (User=381)
2026-01-13T18:08:06.081+09:00  WARN 6033 --- [kicksync_backend] [io-8080-exec-88] b.k.c.exception.GlobalExceptionHandler   : CustomException occurred: [Code: INSUFFICIENT_STOCK, Message: 재고가 부족합니다.]
2026-01-13T18:08:06.083+09:00  WARN 6033 --- [kicksync_backend] [io-8080-exec-88] .m.m.a.ExceptionHandlerExceptionResolver : Resolved [be.kicksync_backend.common.exception.CustomException]
2026-01-13T18:08:06.083+09:00  INFO 6033 --- [kicksync_backend] [o-8080-exec-131] b.k.common.aop.DistributedLockAop        : [Redisson Lock] 락 해제 완료: MultiLock([LOCK:product:1]) (User=381)
```


<br>

### 2. Redis 캐싱을 통한 조회 성능 236% 향상
**상황 및 문제:**
- **I/O 병목**: 전체 상품 목록 조회 API의 높은 호출 빈도로 불필요한 I/O 자원 낭비
- **DB 부하:** 트래픽 급증 시 반복적인 단순 조회 쿼리로 인해 DB 커넥션 풀 고갈 위험 확인.

**해결 전략:**
1.  **Look-aside 캐싱 전략 (`@Cacheable`):**
    -   API 호출 시 Redis 우선 확인 후 Cache Miss 발생 시에만 DB 접근
2.  **데이터 정합성 보장 (`@CacheEvict`):**
    -   관리자에 의한 상품 정보 변경 시 관련 캐시 그룹(products) 전체 무효화로 데이터 일관성 유지

<br>

**성능 개선 결과 (페이징 조회 기준):**

nGrinder 부하 테스트 결과 TPS 약 3.3배 증가 및 응답 시간 73% 단축

| 지표 | 캐시 적용 전 | 캐시 적용 후 | 개선 효과 |
| :--- | :--- | :--- | :--- |
| **TPS (평균 처리량)** | 1,736.7 | **5,831.9** | **약 236% 향상** |
| **Peak TPS** | 2,194.5 | **6,698.0** | **205% 향상** |
| **Mean Test Time** | 26.70ms | **6.99ms** | **73.8% 단축** |
| **Total Executed Tests** | 515,159 | **1,728,652** | **235% 처리량 증가** |



### nGrinder 부하 테스트 결과 스크린샷

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
