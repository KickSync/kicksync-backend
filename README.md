# [ KickSync ] 대용량 트래픽을 감당하는 선착순 한정판 거래 플랫폼

> **핵심 가치**
> * **지속적인 아키텍처 고도화를 통해 100만 건 정산 시간을 2시간 23분 → 1.1초로 단축**하고, 트래픽 폭주 상황에서도 **데드락 없이 재고 정합성을 보장**하는 고가용성 시스템을 구축했습니다.

> **핵심 성과 요약**
> * **배치 성능 개선:** DB 레벨 집계 및 Spring Batch 파티셔닝을 통해 100만 건 정산 시간 **2시간 23분 → 1.1초** 단축
> * **동시성 제어:** `Redisson MultiLock` 및 트랜잭션 경계 분리 적용으로 동시 접속 상황 재고 오차율 **0%** 달성
> * **조회 성능 최적화:** 인덱스 튜닝 및 캐싱(Look-aside), Rate Limiter 적용으로 **TPS 24배 향상 (98 → 2,406)** 및 **응답 속도 96% 단축 (9.2s → 0.36s)**

<br>

<div align="left">
  <h3>API 테스트 & 문서 (Swagger UI)</h3>
  
  <a href="http://134.185.116.180/swagger-ui/index.html#/">
  <img src="https://img.shields.io/badge/Swagger_UI-Live_Test-85EA2D?style=for-the-badge&logo=swagger&logoColor=black" alt="Swagger UI" />
</a>
</div>

> 버튼을 통해 API를 직접 호출해 보실 수 있습니다.
> 로그인 후 발급된 Access Token을 Authorize 버튼에 입력하여 테스트 가능합니다.

<br><br>

## 1. 프로젝트 소개

**[ KickSync ]** 는 대규모 트래픽과 데이터가 발생하는 이커머스 환경(KREAM, StockX 등)에서의 **안정성과 성능 최적화**에 주력한 백엔드 프로젝트입니다.

플랫폼 성장에 따라 급증하는 트래픽과 정산 데이터를 효율적으로 처리하기 위해, **"시스템 확장성 확보"와 "데이터 정합성 보장"** 을 최우선 엔지니어링 목표로 설정했습니다.

### 주요 기능

* **Commerce (주문 및 동시성 제어):**
    * **동시성 제어:** 선착순 구매 시 발생하는 재고 충돌 및 데드락을 방지하기 위해, **Redisson MultiLock**과 **커스텀 AOP(트랜잭션 분리)** 기반의 동시성 제어 컴포넌트 구축
    * **정산 시스템:** 다중 입점사 통합 결제를 위한 Order Splitting 아키텍처 설계 및 PortOne API 교차 검증을 통한 결제 무결성 확보

* **Settlement (대규모 배치 정산):**
    * **대용량 배치:** 100만 건 이상의 정산 데이터를 **도메인 반정규화**와 **DB 레벨 집계(Group By)** 로 압축하고, **Spring Batch Partitioning**을 통해 병렬 처리
    * **장애 허용(Fault Tolerance):** 일시적 결함으로 인한 전체 롤백을 막기 위해, `Skip` 및 `Retry` 기반의 에러 격리 로직 적용

* **Auth & Security (인증 및 방어막):**
    * **Stateless:** **JWT** 기반의 인증 구조로 권한을 분리하고, **Redis**를 활용한 로그아웃(Blacklist) 및 토큰 재발급(Refresh Token) 구현
    * **Rate Limiter:** 악성 봇 및 새로고침 트래픽 방어를 위해 Redis `INCR` 기반의 Rate Limiter를 배치하여 선제적 장애 격리

* **Architecture (분산 환경 & 통제):**
    * **데이터 일관성:** **ShedLock**으로 스케줄러 중복 실행을 방지하여 데이터 무결성 보호
    * **AI 컨텍스트 제어:** 레거시 분리 과정에서 발생하는 휴먼 에러를 줄이기 위해, `MEMORY.md` 기반의 역할 분할 AI 파이프라인을 구축하여 아키텍처 가드레일 확립


<br><br>

## 2. 아키텍처 및 핵심 프로세스

### 2-1. 시스템 아키텍처
<img width="100%" alt="KickSync System Architecture" src="https://github.com/user-attachments/assets/318e28a2-1d6c-498a-b7e0-7826d9182e79" />

### 2-2. 배치 프로세스 아키텍처
<img width="100%" alt="Batch Process Architecture" src="https://github.com/user-attachments/assets/7e991801-31e0-41a3-aa0e-928188e5eba3" />

### 2-3. 핵심 서비스 흐름
<img width="1222" height="952" alt="image" src="https://github.com/user-attachments/assets/a5dcec06-ac7e-48b3-bdfc-096b85d7f41b" />

<br><br>

## 3. 기술 스택

| Category | Technology | Reason for Selection |
| --- | --- | --- |
| **Language** | <img src="https://img.shields.io/badge/Java_21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white"> | Virtual Threads 등 고성능 처리를 위한 최신 기능 지원 및 안정적인 생태계 활용 |
| **Framework** | <img src="https://img.shields.io/badge/Spring_Boot_3.5.5-6DB33F?style=for-the-badge&logo=spring&logoColor=white"> <img src="https://img.shields.io/badge/Spring_Batch-6DB33F?style=for-the-badge&logo=spring&logoColor=white"> <img src="https://img.shields.io/badge/Vue.js_3-4FC08D?style=for-the-badge&logo=vue.js&logoColor=white"> | Chunk 지향 처리를 통한 대용량 데이터의 메모리 효율성 확보 및 Job Repository 기반의 배치 실패 이력 관리 용이 |
| **Database** | <img src="https://img.shields.io/badge/MySQL_8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white"> <img src="https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white"> | ACID 트랜잭션을 통한 데이터 무결성 보장(MySQL) 및 인메모리 기반의 고속 캐싱과 분산 락 활용(Redis) |
| **API Specs** | <img src="https://img.shields.io/badge/Swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=black"> | 즉각적인 API 테스트 및 디버깅 환경 구축과 코드 변경에 따른 문서 자동 최신화 지원 |
| **ORM** | <img src="https://img.shields.io/badge/Spring_Data_JPA-6DB33F?style=for-the-badge&logo=spring&logoColor=white"> <img src="https://img.shields.io/badge/Hibernate-59666C?style=for-the-badge&logo=hibernate&logoColor=white"> | 객체 지향적 도메인 설계와 생산성 확보, `Bulk Insert` 등 쿼리 최적화 용이 |
| **Infra** | <img src="https://img.shields.io/badge/Oracle_Cloud-F80000?style=for-the-badge&logo=oracle&logoColor=white"> <img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white"> | OCI 프리티어를 활용하여 비용 부담 없이 고사양(4 OCPU, 24GB RAM) 테스트 서버 구축 및 지속 가능한 운영 환경 확보 |
| **Test & Monitor** | <img src="https://img.shields.io/badge/nGrinder-FFA500?style=for-the-badge&logo=java&logoColor=white"> <img src="https://img.shields.io/badge/Scouter-00C7B7?style=for-the-badge&logo=scouter&logoColor=white"> | 정량적 지표(TPS, Latency)를 기반으로 병목 구간을 탐지하고 최적화 성과를 검증 |

<br><br>

## 4. 기술적 고도화

> **"가설 - 검증 - 개선"**: 데이터 규모 증가와 트래픽 병목을 해결하기 위해 고민하고 의사결정한 과정입니다.

### [ Phase 1 ] 대용량 정산 병목 해결: 100만 건 처리 시간 2h 23m → 1.1s 단축

**Q. 일일 100만 건의 판매 데이터를 제한된 시간 내에 안정적으로 정산할 수 있는가?**

* **문제 상황:** 초기 `JpaPagingItemReader`를 활용한 단일 스레드 방식으로는 100만 건 처리에 **2시간 23분**이 소요되어, 정산 지연 리스크 및 OOM(Out Of Memory) 위험 발생
* **원인 분석:** 1. 페이징 쿼리의 `OFFSET` 부하 및 런타임 내 실시간 계산(`OR` 조건절)으로 인한 Full Table Scan 발생
  2. 100만 건의 데이터를 애플리케이션 메모리로 모두 가져오는 데서 발생하는 물리적 네트워크 I/O 병목

* **해결 전략 (3단계 최적화):**
  1. **주문 중심 도메인 재설계:** 정산의 주체를 결제(Payment)에서 주문(Order)으로 분리하여, 1:N 다중 입점사 정산을 유연하게 수용하는 반정규화 구조 도입
  2. **DB 집계 쿼리 최적화:** 복잡한 런타임 연산을 제거하고, DB 레벨의 `SUM/GROUP BY` 집계 쿼리를 적용. 100만 건의 데이터를 입점사 수(수백 건) 수준으로 압축하여 애플리케이션으로 이관되는 I/O 최소화
  3. **Spring Batch 파티셔닝:** `PartnerId` 범위를 기준으로 데이터를 10등분 하여, 10개의 워커 스레드가 I/O를 병렬로 처리하도록 구조화

* **최종 성과:**
  * 처리 시간: **2시간 23분 → 1.1초**로 단축 달성
  * 작업 성격을 'Heavy Read/Write(건별 처리)'에서 'Light Aggregated Read(DB 집계 후 결과 읽기)'로 전환하여 수평적 확장성 확보

<br>

### [ Phase 2 ] 동시 접속 상황에서의 재고 정합성 100% 보장

**Q. 인기 상품 발매 시 순간적으로 몰리는 동시 주문 환경에서, 초과 판매 및 데드락을 방어할 수 있는가?**

* **문제 상황:** nGrinder VUser 500명 동시 주문 테스트 결과, 재고가 음수(-N)로 떨어지는 갱신 손실(Lost Update) 및 다중 상품 결제 시의 데드락(Deadlock) 위험 확인

* **기술적 의사결정 및 해결 과정:**
  1. **Redisson 분산 락 채택:** DB 커넥션 풀 보호를 최우선으로 고려. 비관적 락 사용 시 대기 스레드 적체로 인한 시스템 전체 병목을 우려하여, Pub/Sub 기반으로 부하가 적은 Redisson 분산 락 채택
  2. **MultiLock 기반 데드락 방지:** 장바구니 결제 시 여러 스레드가 서로 다른 순서로 락을 획득하려다 발생하는 순환 대기(Circular Wait)를 막기 위해, ID 리스트를 정렬한 후 원자적으로 점유하는 MultiLock 구조 구현
  3. **트랜잭션 경계 분리 (Custom AOP):** `@Transactional` 커밋 전에 락이 먼저 풀려 발생하는 동시성 틈새를 막고자, 커스텀 AOP를 도입하여 **'락 획득 → 트랜잭션 진입 → 커밋 완료 → 락 해제'** 의 생명주기를 물리적으로 강제

* **검증 결과:**
  * 동시 요청 500건 테스트 시 **초과 판매 0건** 달성 (정합성 100%)
  * Fail-fast 방식을 통해 애플리케이션 레벨에서 불필요한 대기를 끊어내어 시스템 가용성 방어

> <details>
> <summary><strong>Redisson 락 동작 로그 및 테스트 환경 보기</strong></summary>
> <div markdown="1">
> <br>
> 
> **1. Redisson 락 동작 로그**
>
> ```log
> // 1. 정상 주문 처리: 락 획득 -> 재고 차감 -> 락 해제
> 18:07:57 INFO [DistributedLockAop] : [Redisson Lock] 락 획득 성공: [LOCK:product:1] (User=178)
> 18:08:02 INFO [OrderService]       : [CONCURRENCY_TEST] 재고 차감: User=178, 남은 재고=9
> 18:08:02 INFO [DistributedLockAop] : [Redisson Lock] 락 해제 완료: [LOCK:product:1] (User=178)
> 
> // 2. 동시성 환경 재고 소진 과정 (Race Condition 제어)
> 18:08:05 INFO [OrderService]       : [CONCURRENCY_TEST] 재고 차감: User=189, 남은 재고=8
> 18:08:05 INFO [OrderService]       : [CONCURRENCY_TEST] 재고 차감: User=13,  남은 재고=7
> ...
> 18:08:06 INFO [OrderService]       : [CONCURRENCY_TEST] 재고 차감: User=36,  남은 재고=0
> 18:08:06 INFO [DistributedLockAop] : [Redisson Lock] 락 해제 완료: [LOCK:product:1] (User=36)
> 
> // 3. 재고 소진 후 접근 시: 락 획득 후 즉시 예외 발생 (Fail-fast)
> 18:08:06 INFO [DistributedLockAop] : [Redisson Lock] 락 획득 성공: [LOCK:product:1] (User=381)
> 18:08:06 WARN [GlobalExceptionHandler] : CustomException: [INSUFFICIENT_STOCK] 재고가 부족합니다.
> 18:08:06 INFO [DistributedLockAop] : [Redisson Lock] 락 해제 완료: [LOCK:product:1] (User=381)
> ```
> <br>
>
> **2. nGrinder 테스트 환경**
> 
> <img width="100%" alt="image" src="https://github.com/user-attachments/assets/13bb98f2-ee53-4533-b184-c9e1f136ef4a" />
>
> </div>
> </details>

### **[ 분산 락 시퀀스 다이어그램 ]**
<img width="1172" height="1181" alt="image" src="https://github.com/user-attachments/assets/46f1c19c-9b81-4d9e-91b1-3b3d779c03ed" />

<br>

<br><br>

### [ Phase 3 ] 조회 성능 24배 개선 및 Rate Limiter를 통한 장애 격리

**Q. 트래픽의 80%가 집중되는 신규 발매 탭 조회 시, DB 부하를 어떻게 제어할 것인가?**

* **문제 상황:** 트래픽 급증 시나리오(VUser 1,000)에서 평균 응답 시간이 **9.2초**까지 치솟으며 DB 커넥션 고갈 발생

* **해결 과정:**
  1. **인덱스 한계 식별:** `release_date` 정렬을 위한 1차 인덱스 튜닝을 진행했으나, 대규모 동시 접속 상황에서는 B-Tree 탐색만으로도 물리적 디스크 I/O 한계에 도달함을 파악
  2. **Look-aside 캐싱 도입:** 1페이지(Hot Data)를 Redis에 캐싱(`TTL 10분`)하여 기본 DB I/O 부하를 제거. 관리자 수정(CUD) 시 `@CacheEvict`로 캐시를 즉시 무효화하여 일관성 보완
  3. **Rate Limiter 구축:** 매크로 및 악의적 새로고침으로부터 DB와 애플리케이션을 보호하기 위해, Redis `INCR` 연산을 활용한 Lock-free Rate Limiter를 서비스 진입점에 배치

* **검증 결과:**
  * **TPS 24배 향상:** 평균 98.9 → 2,406.3
  * **응답 속도 96% 단축:** 평균 9.2s → 0.36s

> (\*) *Redis 적용 시 발생한 에러 379건은 TPS 폭증으로 인한 로컬 테스트 환경의 네트워크 포트 고갈 이슈로 확인*
   
   > <details>
   > <summary><strong>[성능 지표] nGrinder 부하 테스트 상세 그래프 확인하기</strong></summary>
   > <div markdown="1">
   > <br>
   >
   > #### 페이징 조회 (40건)
   >
   > **[ Step 1. DB Only (No Index) ] - 서비스 붕괴**
   > 
   ><img width="2048" height="614" alt="image" src="https://github.com/user-attachments/assets/9354df12-abeb-4880-8c64-ae9110c0a223" />
   >
   > **[ Step 2. Index Tuning ] - 성능 개선되었으나 여전히 병목 존재**
   > 
   > <img width="2048" height="615" alt="image" src="https://github.com/user-attachments/assets/71e6f898-5ae1-4cc7-a399-4b36c401b95a" />
   >
   > **[ Step 3. Redis Caching ] - 압도적인 처리량 및 응답 속도 확보**
   > 
   > <img width="2048" height="602" alt="image" src="https://github.com/user-attachments/assets/871381ef-105b-4d1a-aa80-a9dbe8145e0d" />
   > </div>
   > </details>

<br><br>

## 5. 트러블 슈팅

### 1. 분산 환경에서의 스케줄러 중복 실행 방지 (ShedLock)
* **문제 상황:** Scale-out(다중 서버) 환경 도입 후, 동일 스케줄러의 중복 실행으로 인해 정산 데이터가 이중 적재되는 무결성 훼손 발생
* **해결:** 별도의 무거운 배치 서버를 구축하는 오버 엔지니어링을 피하고, 기존 DB 자원을 활용하여 메타데이터 테이블만으로 락 관리가 가능한 **ShedLock**을 도입. 인스턴스 수와 무관하게 단일 서버 수행을 보장했습니다.

### 2. 대량 데이터 적재 성능 최적화 (Bulk Insert)
* **문제 상황:** JPA `saveAll()` 사용 시, MySQL `IDENTITY` 전략의 한계로 인해 Batch Insert가 작동하지 않아 10만 건 저장에 약 20초 소요
* **해결:** 정산 데이터 저장은 영속성 컨텍스트(Dirty Checking 등) 관리가 불필요한 Write-only 작업임을 인지. JPA의 편의성을 포기하는 대신 `JdbcTemplate` 기반의 **Bulk Insert**로 전환하여 처리 시간을 **20.2s → 0.67s(96% 개선)** 로 단축했습니다.

<br><br>

## 6. 설계 회고 및 한계 분석

### 1. 배치 처리의 자원 격리 전략과 한계
* **설계:** 100만 건 파티셔닝 로직의 CPU/DB I/O 점유를 고려하여, 트래픽 유휴 시간인 새벽 04:00에 배치를 실행하고 `GridSize`를 제한하여 자원 경합을 막았습니다.
* **한계 및 개선안:** 현재는 온라인 DB와 배치 처리가 물리적으로 같은 자원을 공유합니다. 향후 데이터가 1,000만 건 이상으로 급증할 경우, 배치 작업은 읽기 전용 복제본(Slave DB)에서 수행하여 Master DB의 부하를 원천 분리하는 CQRS 기반 아키텍처로 개선할 계획입니다.

### 2. 단일 장애 지점(SPOF) 리스크와 Fallback
* **설계:** 동시성 제어를 위해 Redis 분산 락을 도입하여 빠르고 가벼운 락 획득을 구현했습니다.
* **한계 및 개선안:** 주문 로직이 Redis에 강결합되어 있어 Redis 노드 다운 시 서비스 장애로 직결됩니다. 이를 방어하기 위해 향후 Redis 장애 시 DB 비관적 락으로 우회하는 **Fallback 메커니즘**을 구축하거나, 물리적인 Redis Cluster 구성을 통해 고가용성을 확보해야 함을 인지하고 있습니다.

<br><br>

## 7. ERD

<img width="2400" height="1582" alt="image" src="https://github.com/user-attachments/assets/176389bd-f7ea-4bd8-a30f-0e08b778ae92" />

* **[ ERD Cloud 링크 바로가기 ](https://www.erdcloud.com/d/B5xBxsPqkP4uwSPt4)**

--------
<br><br>

