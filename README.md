# [ KickSync ] 대용량 트래픽을 감당하는 선착순 한정판 거래 플랫폼

> **핵심 가치**
> * **일일 100만 건 이상의 데이터 처리 속도를 136배 단축**하고 **트래픽 폭주 상황에서도 재고 정합성 100%를 보장**하는 고가용성 아키텍처를 설계했습니다.
> 
> 

> **핵심 성과 요약**
> * **배치 성능 개선:** Partitioning 기반 병렬 처리로 100만 건 정산 시간 **2시간 23분 → 1분 2초 (약 136배 단축)**
> * **동시성 제어:** `Redisson` 분산 락 도입으로 동시 접속 상황 재고 오차율 **0%** 달성 → **(초과 결제로 인한 환불/CS 리스크 제거)**
> * **조회 성능:** Look-aside 캐싱 전략으로 TPS **1,736 → 5,831 (약 3.3배 증가)** 및 응답 속도 **73% 개선**
> 
> 

<br>

## 1. 프로젝트 소개

**[ KickSync ]** 는 입점사 기반 플랫폼(KREAM, StockX)을 모델로 하여, 대규모 트래픽과 데이터가 발생하는 환경에서의 **안정성과 성능 최적화**에 주력한 백엔드 프로젝트입니다.

플랫폼 성장에 따라 급증하는 트래픽과 정산 데이터를 효율적으로 처리하기 위해 **"시스템이 확장에 유연한가?", "데이터 정합성은 보장되는가?"** 를 검증하며 엔지니어링 문제를 해결했습니다.

### 주요 기능

* **Commerce (주문 및 결제 흐름):**
    * **동시성 제어:** 한정판 신발의 선착순 구매 시 발생하는 재고 충돌(Race Condition)을 **Redisson 분산 락**으로 해결하여 초과 주문 방지.
    * **무결성 검증:** **PortOne** API 연동을 통해 결제 요청 금액과 승인 금액을 교차 검증하고 **주문 취소/반품** 시 트랜잭션 단위로 정확한 환불 처리 보장.

* **Settlement (입점사 정산 시스템):**
    * **대용량 배치:** 크림/무신사와 같은 **입점사별 수수료 정책**을 적용하여 매일 발생하는 대규모 판매 데이터를 **Spring Batch Partitioning**으로 병렬 정산 처리.
    * **성능 최적화:** 정산 데이터 집계 시 **Bulk Insert**와 **Chunk** 지향 처리를 통해 배치 수행 시간을 단축.

* **Auth (인증 및 보안):**
    * **Stateless:** **JWT** 기반의 인증 구조로 입점사/구매자/관리자 권한을 분리하고 **Redis**를 활용한 로그아웃(Blacklist) 및 토큰 재발급(Refresh Token) 구현.
--------

<br><br>

## 2. 아키텍처 및 핵심 프로세스

### 2-1. 시스템 아키텍처

<img width="100%" alt="KickSync System Architecture" src="https://github.com/user-attachments/assets/318e28a2-1d6c-498a-b7e0-7826d9182e79" />

### 2-2. 배치 프로세스 아키텍처

<img width="100%" alt="Batch Process Architecture" src="https://github.com/user-attachments/assets/7e991801-31e0-41a3-aa0e-928188e5eba3" />

### 2-3. 핵심 서비스 흐름

<img width="1222" height="952" alt="image" src="https://github.com/user-attachments/assets/a5dcec06-ac7e-48b3-bdfc-096b85d7f41b" />

--------

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


--------
<br><br>

## 4. 기술적 고도화

> **"가설 - 검증 - 개선"**: 데이터 규모 증가와 트래픽 병목을 해결하기 위해 고민하고 의사결정한 과정입니다.

### [ Phase 1 ] 100만 건 정산 데이터 처리 속도 136배 단축

**Q. 매일 쌓이는 100만 건의 판매 데이터를 어떻게 정해진 시간 내에 정산할 것인가?**

* **문제 상황:** 초기 `JpaPagingItemReader` 방식으로는 100만 건 처리에 **2시간 23분**이 소요되어, 정산 지연 리스크 및 Out Of Memory 위험이 발생함.
* **원인 분석:**
  1. **I/O 병목:** 페이징 쿼리의 `OFFSET` 증가로 인한 DB 부하 및 `filesort` 발생.
  2. **리소스 유휴:** 단일 스레드 처리로 인해 CPU와 DB 자원을 충분히 활용하지 못함.


* **해결 과정:**
  * **Step 1 (인덱싱):** 커버링 인덱스 적용으로 쿼리 실행 계획에서 `filesort` 제거 → **19분대 진입 (약 7.3배 개선)**
  * **Step 2 (멀티 스레드):** `ThreadPoolTaskExecutor`를 적용했으나 I/O 대기 시간으로 인해 성능 향상 미미 (1.1배) → **CPU가 아닌 I/O가 병목임을 파악.**
  * **Step 3 (파티셔닝):** **Spring Batch Partitioning**을 도입하여 데이터 범위를 분할하고 I/O를 병렬로 처리 (`GridSize=10`) → **최종 1분대 진입.**


* **최종 성과:**
  * 처리 시간: **2시간 23분 → 1분 2초 (약 136배 단축)**
  * 시스템 자원을 최대로 활용하며 수평적 확장이 가능한 배치 아키텍처 확보.
    
  * **정량적 성과 (nGrinder 부하 테스트 결과)**
    | 최적화 단계 | 실행 시간 (100만 건) | 이전 대비 향상 | 핵심 성과 |
    | --- | --- | --- | --- |
    | **1. Paging Reader** | 2시간 23분 | - | 메모리 안정성 확보하였으나 **I/O 병목 발생** |
    | **2. + 커버링 인덱스** | 19분 34초 | **7.3배** | `filesort` 제거 및 **I/O 최적화 기반 마련** |
    | **3. + 멀티 스레드** | 17분 37초 | **1.1배** | CPU 병렬 처리 한계 확인 → **I/O가 핵심 병목임을 증명** |
    | **4. + 파티셔닝** | **1분 2초** | **18.9배** | **I/O 병렬 처리로 병목 해결**, 수평적 확장성 확보 |

   - **OOM 발생부터 파티셔닝 적용까지의 전체 과정**: [Velog 포스팅 바로가기](https://velog.io/write?id=28f459ea-74a8-4c89-af6c-668413d411b4)
      - **주요 내용**: `JpaPagingItemReader` 한계 분석, 커버링 인덱스 적용 전후 비교, 멀티 스레드 vs 파티셔닝 비교

<br>

### **[ 파티셔닝 아키텍처 구조도 ]**

<img width="100%" alt="Partitioning Architecture" src="https://github.com/user-attachments/assets/0d74dd20-e17a-4dcd-ab02-d39a4c51d9d6" />

<br><br><br>

### [ Phase 2 ] 동시 접속 상황에서의 재고 정합성 100% 보장

**Q. 인기 상품 발매 시 수천 명의 동시 요청이 발생한다면? (Race Condition)**

* **문제 상황:** nGrinder 부하 테스트 결과, 재고가 음수(-N)로 떨어지는 **초과 판매** 현상 발생.
* **기술적 의사결정:**
  * **Java Synchronized:** 다중 서버 환경(Scale-out)에서 인스턴스 간 동기화 불가 → **[기각]**
  * **Pessimistic Lock (DB):** 확실한 데이터 보호는 가능하나, 데드락 위험 및 대기 시간 증가로 인한 트래픽 병목 우려 → **[보류]**
  * **Redis Distributed Lock (Redisson):** Pub/Sub 방식을 사용하여 Redis 부하를 줄이면서 분산 환경 제어 가능 → **[채택]**

* **검증 결과:**
  * 동시 요청 500건 테스트 시 **재고 오차 0건** 달성.
  * `WaitTime`과 `LeaseTime` 설정을 통해 데드락 방지 및 UX 고려.

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
   <br><br>

### [ Phase 3 ] 캐싱 전략을 통한 조회 성능 236% 개선

**Q. 단순 조회 쿼리가 DB 커넥션을 점유하여 전체 장애를 유발한다면?**

* **문제 상황:** 전체 상품 목록 조회 API 호출이 빈번하여 DB CPU 부하가 증가하고, 트래픽 급증 시 커넥션 풀 고갈 위험이 감지됨.
* **해결 전략:** **Look-aside Caching** 전략 적용.
  * Redis를 우선 조회하고, Cache Miss 발생 시에만 DB에 접근하여 부하를 분산.
  * `@CacheEvict`를 통해 데이터 업데이트 시 관련 캐시를 즉시 무효화하여 데이터 일관성 유지.


* **검증 결과 및 판단 근거:**
* **결과:** nGrinder 부하 테스트 결과, TPS는 **1,736 → 5,831 (약 3.3배)** 로 폭발적으로 증가했으며, 평균 응답 시간은 **26.7ms → 6.99ms (73% 단축)** 로 개선됨.
  * **Trade-off:** 캐시 도입 시 원본 데이터(DB)와 캐시 데이터(Redis) 간의 **정합성 불일치** 가 발생할 수 있으며, Redis 인프라 비용이 추가됨.
  * **판단 근거:** '상품 목록'은 결제나 재고처럼 실시간 강한 정합성이 요구되는 도메인이 아닌, 다수의 사용자에게 빠르게 노출되어야 하는 **전시 도메인**입니다. 따라서 **미세한 데이터 반영 지연보다는 응답 속도 확보와 DB 보호가 비즈니스적으로 더 중요하다고 판단**하여 캐싱을 적용했습니다.


* **정량적 성과 (nGrinder 부하 테스트 결과)**
  | 지표 | 캐시 적용 전 | 캐시 적용 후 | 개선 효과 |
  | --- | --- | --- | --- |
  | **TPS (평균 처리량)** | 1,736.7 | **5,831.9** | **약 236% 향상** |
  | **Peak TPS** | 2,194.5 | **6,698.0** | **약 205% 향상** |
  | **Mean Test Time** | 26.70ms | **6.99ms** | **약 73.8% 단축** |
  | **Total Executed Tests** | 515,159 | **1,728,652** | **약 235% 증가** |

   > <details>
   > <summary><strong>[성능 지표] nGrinder 부하 테스트 상세 그래프 확인하기</strong></summary>
   > <div markdown="1">
   > <br>
   >
   > #### 1. 전체 상품 조회 (1,000건)
   >
   > **[ 캐시 미적용 ]**
   > <img width="100%" alt="image" src="https://github.com/user-attachments/assets/a8a60f55-ded3-463d-a12f-52f7bea0e1bd" />
   >
   > **[ 캐시 적용 ]**
   > <img width="100%" alt="image" src="https://github.com/user-attachments/assets/d146f350-f8ba-4463-8479-955a9f7e6e08" />
   >
   > <br>
   >
   > #### 2. 페이징 조회 (40건) - *Main Result*
   >
   > **[ 캐시 미적용 ]**
   > <img width="100%" alt="image" src="https://github.com/user-attachments/assets/93da321c-1319-4b59-a299-cbcc1e1f3dd7" />
   >
   > **[ 캐시 적용 ]**
   > <img width="100%" alt="image" src="https://github.com/user-attachments/assets/d667326a-7e62-499d-bcdb-b29fc57f3255" />
   >
   > </div>
   > </details>

--------
<br><br>

## 5. 트러블 슈팅

### 1. 분산 환경에서의 스케줄러 중복 실행 방지 (ShedLock)

* **문제 상황:** 서비스 규모 확장을 위해 **서버 대수를 여러 대로 늘리자** 각 서버가 정해진 시간에 동일한 정산 작업을 동시에 수행하여 **데이터가 중복 생성되는 무결성 사고** 발생.
* **해결 전략:** DB 기반의 분산 락 라이브러리인 **ShedLock** 도입.
* **결과:** 서버 대수와 관계없이 **단 하나의 서버만 작업을 수행하도록 제어**하여 데이터 중복 발생 가능성 원천 차단.

<br>

### 2. JPA 대량 Insert 성능 문제 해결 (Bulk Insert)

* **문제 상황:** JPA `saveAll()`로 10만 건 데이터 저장 시 약 20초 소요.
* **원인:** MySQL의 `IDENTITY` 전략 사용 시, JPA는 PK 값을 알기 위해 **Insert 쿼리를 매번 실행(단건 처리)** 하므로 배칭 최적화가 불가능함.
* **해결 전략:** `JdbcTemplate`을 활용한 **Bulk Insert**로 전환.
* **결과:** 처리 시간 20.2s → **0.67s (약 96% 성능 개선)**

--------
<br><br>


## 6. 설계 회고 및 한계 분석

### 1. 배치 처리의 자원 격리 전략과 한계

* **설계 전략:**
     * **Time-Window Scheduling:**
       * 100만 건 이상의 대량 데이터를 처리하는 파티셔닝 로직은 CPU와 Disk I/O를 집중적으로 사용합니다.
       * 이를 고려하여 실사용자 트래픽이 가장 적은 **새벽 04:00**에 배치를 실행, 온라인 서비스의 성능 저하를 방지했습니다.
     * **GridSize 최적화:** DB Connection Pool의 여유분을 계산하여 `GridSize=10`으로 설정, 자원을 최대한 활용하되 고갈시키지 않도록 제한했습니다.

* **한계 및 리스크:**
     * 현재 구조는 온라인 DB와 배치 처리가 **물리적으로 같은 DB 자원**을 공유합니다.
     * 만약 데이터가 1,000만 건 이상으로 급증하여 배치 수행 시간이 길어질 경우, 아침 시간대 실사용자 트래픽과 경합이 발생할 위험이 있습니다.

* **향후 개선안:**
     * **Reader DB 분리:** 배치 작업은 읽기 전용 복제본(Slave DB)에서 수행하여 Master DB의 부하를 제거하는 구조로 개선할 계획입니다.

<br>

### 2. Redis 강결합 구조와 단일 장애 지점 리스크 관리

* **설계 전략:** 재고 정합성을 위해 `Redisson` 분산 락을 도입했습니다. DB 락 대비 가볍고 빠른 처리가 가능하여 트래픽 폭주 상황에서도 안정적인 응답 속도를 확보했습니다.

* **한계:**
     * **단일 장애 지점:** 주문 로직이 Redis에 강하게 의존하고 있어, Redis 서버 장애 시 주문 기능 전체가 마비되는 리스크가 존재합니다.

* **향후 개선안:**
     * **Fallback 메커니즘 구축:** Redis 연결 실패 시 **"DB 비관적 락"** 으로 자동 전환되어, 성능은 다소 저하되더라도 주문 기능은 정상 동작하도록 가용성을 확보할 예정입니다.
     * **Redis Cluster:** 단일 노드가 아닌 클러스터 구성을 통해 고가용성을 물리적으로 확보해야 함을 인지했습니다.

<br>

### 3. 데이터 무결성과 성능 사이의 Trade-off (캐싱)

* **설계 전략:** '상품 목록 조회'는 데이터의 실시간성보다 **조회 성능**이 비즈니스 매출에 직결된다고 판단하여, `Look-aside` 캐싱 전략을 적용했습니다.

* **한계:** `@CacheEvict`를 적용했으나, 네트워크 지연 등으로 인해 캐시 갱신이 실패할 경우 DB와 캐시 간의 **데이터 불일치** 가 발생할 수 있는 순간이 존재합니다.

* **향후 개선안:** 데이터 불일치 허용 범위를 명확히 정의하고, 만료 시간을 짧게 가져가되 **Refresh Ahead** 전략을 혼합하여 정합성과 성능의 균형점을 지속적으로 튜닝할 계획입니다.

--------
<br><br>

## 7. ERD (최신화 예정)

<img width="100%" alt="ERD" src="https://github.com/user-attachments/assets/5dace7c6-d2d5-4f9c-92ac-2b5bbc95f64e" />

* **[ ERD Cloud 링크 바로가기 ](https://www.erdcloud.com/d/B5xBxsPqkP4uwSPt4)**

--------
<br><br>

