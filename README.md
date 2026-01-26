# [ KickSync ] 대용량 트래픽을 감당하는 선착순 한정판 거래 플랫폼

> **핵심 가치**
> * **일일 100만 건 이상의 데이터 처리 속도를 136배 단축**하고 **트래픽 폭주 상황에서도 재고 정합성 100%를 보장**하는 고가용성 아키텍처를 설계했습니다.
> 
> 

> **핵심 성과 요약**
> * **배치 성능 개선:** Partitioning 기반 병렬 처리 도입, 100만 건 정산 시간 **2시간 23분 → 1분 2초 (약 136배 단축)**
> * **동시성 제어:** `Redisson` 분산 락 도입으로 동시 접속 상황 재고 오차율 **0%** 달성 → **(초과 결제 방지)**
> * **조회 성능:** Look-aside 캐싱 전략으로 TPS **1,736 → 5,831 (약 3.3배 증가)** 및 응답 속도 **73% 개선**
> 
>

<br>

<div align="left">
  <h3>API 테스트 & 문서 (Swagger UI)</h3>
  
  <a href="http://134.185.116.180/swagger-ui/index.html#/">
  <img src="https://img.shields.io/badge/Swagger_UI-Live_Test-85EA2D?style=for-the-badge&logo=swagger&logoColor=black" alt="Swagger UI" />
</a>
</div>

> 버튼을 통해 API를 직접 호출해보실 수 있습니다.
> 
> 로그인 후 발급된 Access Token을 Authorize 버튼에 입력하여 테스트 가능합니다.

<br><br>

## 1. 프로젝트 소개

**[ KickSync ]** 는 입점사 기반 플랫폼(KREAM, StockX)을 벤치마킹하여 대규모 트래픽과 데이터가 발생하는 환경에서의 **안정성과 성능 최적화**에 주력한 백엔드 프로젝트입니다.

플랫폼 성장에 따라 급증하는 트래픽과 정산 데이터를 효율적으로 처리하기 위해 **"시스템 확장성 확보"와 "데이터 정합성 보장"** 을 최우선 엔지니어링 목표로 설정했습니다.

### 주요 기능

* **Commerce (주문 및 결제 흐름):**
    * **동시성 제어:** 선착순 구매 시 발생하는 재고 충돌(Race Condition)을 **Redisson 분산 락**으로 제어하여 초과 주문 원천 차단
    * **정산 시스템:** **다중 입점사 통합 결제를 위한 Order Splitting** 아키텍처 설계 및 PortOne API 교차 검증을 통한 **결제 무결성 확보**

* **Settlement (입점사 정산 시스템):**
    * **대용량 배치:** 크림/무신사와 같은 **입점사별 수수료 정책**을 적용하여 매일 발생하는 대규모 판매 데이터를 **Spring Batch Partitioning**으로 병렬 정산 처리
    * **성능 최적화:** 정산 데이터 집계 시 **Bulk Insert**와 **Chunk** 지향 처리를 통해 배치 수행 시간 단축

* **Auth (인증 및 보안):**
    * **Stateless:** **JWT** 기반의 인증 구조로 입점사/구매자/관리자 권한을 분리하고 **Redis**를 활용한 로그아웃(Blacklist) 및 토큰 재발급(Refresh Token) 구현
 
* **Architecture (분산 환경):**
    * **데이터 일관성:** **ShedLock**으로 스케줄러 중복 실행을 방지하고 **Pub/Sub 패턴**을 도입하여 서비스 간 결합도 최소화
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

**Q. 일일 100만 건의 판매 데이터를 제한된 시간 내에 정산할 수 있는가?**

* **문제 상황:** 초기 `JpaPagingItemReader` 방식으로는 100만 건 처리에 **2시간 23분**이 소요되어, 정산 지연 리스크 및 Out Of Memory 위험 발생
* **원인 분석:**
  1. **I/O 병목:** 페이징 쿼리의 `OFFSET` 증가로 인한 DB 부하 및 `filesort` 발생
  2. **리소스 유휴:** 단일 스레드 처리로 인한 CPU/DB 자원 활용률 저조


* **해결 과정:**
  * **Step 1 (인덱싱):** 커버링 인덱스 적용으로 쿼리 실행 계획에서 `filesort` 제거 → **19분대 진입 (약 7.3배 개선)**
  * **Step 2 (멀티 스레드):** `ThreadPoolTaskExecutor`를 적용했으나 성능 개선 미미 (1.1배)
     * **Scouter 모니터링 결과 스레드 증가에도 CPU 사용률이 저조함을 확인하여** 병목의 핵심이 연산 처리가 아닌 DB I/O 대기 임을 데이터로 입증
  * **Step 3 (파티셔닝):** **Spring Batch Partitioning**을 도입하여 데이터 범위를 분할하고 I/O를 병렬로 처리 (`GridSize=10`) → **최종 1분대 진입**


* **최종 성과:**
  * 처리 시간: **2시간 23분 → 1분 2초 (약 136배 단축)**
  * 시스템 자원을 최대로 활용하며 수평적 확장이 가능한 배치 아키텍처 확보
    
  * **정량적 성과**
    | 최적화 단계 | 실행 시간 (100만 건) | 이전 대비 향상 | 핵심 성과 |
    | --- | --- | --- | --- |
    | **1. Paging Reader** | 2시간 23분 | - | 메모리 안정성 확보하였으나 **I/O 병목 발생** |
    | **2. + 커버링 인덱스** | 19분 34초 | **7.3배** | `filesort` 제거 및 **I/O 최적화 기반 마련** |
    | **3. + 멀티 스레드** | 17분 37초 | **1.1배** | CPU 병렬 처리 한계 확인 → **I/O가 핵심 병목임을 증명** |
    | **4. + 파티셔닝** | **1분 2초** | **18.9배** | **I/O 병렬 처리로 병목 해결**, 수평적 확장성 확보 |

   - **OOM 발생부터 파티셔닝 적용까지의 전체 과정**: [Velog 포스팅 바로가기](https://velog.io/@gminnimk/Batch-System)
      - **주요 내용**: `JpaPagingItemReader` 한계 분석, 커버링 인덱스 적용 전후 비교, 멀티 스레드 vs 파티셔닝 비교

<br>

### [ Phase 1-1 ] 아키텍처 재설계: 비즈니스 확장성과 조회 성능의 동시 확보

> **"Read 성능을 위해 Write 시점에 구조를 최적화"**
> <br>
> 파티셔닝으로 1분대 진입에 성공했으나 **"다중 입점사 결제 시 정산 데이터의 정합성을 어떻게 보장할 것인가?"** 라는 비즈니스 확장성 고민에서 시작된 고도화 과정입니다.

#### Q. 기존 구조는 대규모 다중 파트너 정산을 수용할 수 있는가?

* **한계점 발견:**
    * **비즈니스 제약:** 기존 `Payment`(결제) 중심 구조는 엔티티 내 PartnerId가 단일 필드로 존재해 **'한 번의 결제에 여러 입점사 상품이 포함된 경우'** 를 수용하지 못하는 구조적 결함 확인
    * **성능 병목:** 결제일과 취소일을 동시에 고려하는 복잡한 `OR` 조건이 파티셔닝 범위와 결합될 때 인덱스 선택도가 낮아져 **Full Table Scan 유발**
    * **Risk:** 데이터 규모 확장 시 쿼리 비용이 기하급수적으로 증가하여 **Read I/O 병목이 재발**할 수밖에 없는 구조적 위험 존재

* **해결 전략:**
    * **도메인 모델 재설계:** "정산의 주체는 결제가 아닌 주문이다"라는 관점으로 전환하여 주문 생성 시점에 입점사별로 주문을 분리 저장하는 **Order Split 아키텍처 도입**
    * **반정규화 및 커버링 인덱스:** 정산 필수 데이터(PartnerId, Status, Price)를 Order 테이블에 직접 기록하고 이를 모두 포함하는 복합 인덱스를 구성하여 **Covering Index Scan 유도**

<br>

**[ 구조 변경으로 15배 더 빨라진 이유 ]**

| 구분 | **Before (Payment 중심)** | **After (Order 중심)** | **핵심 차이** |
| :--- | :--- | :--- | :--- |
| **정산 관점** | "결제 성공 건을 집계" | **"주문 확정 건을 집계"** | 도메인 주체 변경 |
| **확장성** | 다중 입점사 정산 처리 불가<br>*(1 결제 = 1 입점사 제약)* | **Order Split으로 다중 수용**<br>*(1 결제 = N 주문 분리 저장)* | 비즈니스 로직 유연화 |
| **조회 방식** | 인덱스 미적용 (Full Scan) | **Covering Index Scan** | I/O 비용 최소화 |
| **최적화** | 비효율적 쿼리로 인한 Read 병목 | **데이터 정렬 및 반정규화** | Read 속도 극대화 |

<br>

* **최종 성과:**
    * 다중 입점사 결제 지원 및 정산 원천 데이터 조회 비용을 **'제로'** 에 가깝게 최적화
    * 처리 시간: **1분 2초 → 3.9초 (약 15.8배 추가 단축)**
    * Phase 1 초기 대비 총 개선율: **2시간 23분 → 3.9초**

<br>

<br>

### **[ 파티셔닝 아키텍처 구조도 ]**

<img width="1211" height="1001" alt="image" src="https://github.com/user-attachments/assets/c7bd7827-308e-4415-a2a2-eead19f9bb46" />

<br><br><br>

### [ Phase 2 ] 동시 접속 상황에서의 재고 정합성 100% 보장

**Q. 인기 상품 발매 시 수천 명의 동시 요청이 발생한다면? (Race Condition)**

* **문제 상황:** nGrinder 부하 테스트 결과, 재고가 음수(-N)로 떨어지는 **초과 판매** 현상 발생
* **기술적 의사결정:**
  * **1. 동시성 제어 방식 선정:**
    * **Java Synchronized:** 다중 서버 환경(Scale-out)에서 인스턴스 간 동기화 불가 → **[기각]**
    * **Pessimistic Lock (DB):** 확실한 데이터 보호는 가능하나 데드락 위험 및 대기 시간 증가로 인한 트래픽 병목 우려 → **[보류]**
    * **Redis Distributed Lock (Redisson):** Pub/Sub 방식을 사용하여 Redis 부하를 줄이면서 분산 환경 제어 가능 → **[채택]**
  
  * **2. Lock 구현체 최저화 (Letturce vs Redisson):**
    * **Lettuce:** Spin Lock 방식으로 락 획득 재시도 시 Redis에 과도한 트래픽 부하 유발
    * **Redisson:** **Pub/Sub 방식**을 지원하여 락 해제 시에만 클라이언트에 알림을 보내는 방식으로 **Redis 부하 최소화 및 대기 효율성 증대**
   
  * **3. 트랜잭션 정합성 확보 (AOP):**
    * **문제:** `@Transactional` AOP가 락 해제보다 늦게 커밋될 경우 다른 스레드가 변경 전 데이터를 읽는 데이터 불일치 발생
    * **해결:** **Custom AOP**를 도입하여 **'락 획득 → 트랜잭션 시작 → 커밋 → 락 해제'** 순서를 강제함으로써 동시성 이슈 원천 차단

* **검증 결과:**
  * 동시 요청 500건 테스트 시 **재고 오차 0건** 달성
  * `WaitTime`과 `LeaseTime` 설정을 통해 데드락 방지 및 UX 고려

<br>

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

### **[ 분산 락 시퀀스 다이어그램 (Lock Facade) ]**
<img width="950" height="740" alt="image" src="https://github.com/user-attachments/assets/46908311-ec98-4e45-af47-d942936be787" />

<br>

   <br><br>

### [ Phase 3 ] 캐싱 전략을 통한 조회 성능 236% 개선

**Q. 단순 조회 쿼리가 DB 커넥션을 점유하여 전체 장애를 유발한다면?**

* **문제 상황:** 전체 상품 목록 조회 API 호출 빈도로 DB CPU 부하가 증가하고 트래픽 급증 시 커넥션 풀 고갈 위험 감지
* **해결 전략:** **Look-aside Caching** 전략 적용
  * Redis를 우선 조회하고 Cache Miss 발생 시에만 DB에 접근하여 부하를 분산
  * `@CacheEvict`를 통해 데이터 업데이트 시 관련 캐시를 즉시 무효화하여 데이터 일관성 유지


* **검증 결과 및 판단 근거:**
   * **결과:** TPS **1,736 → 5,831 (약 3.3배)** 및 평균 응답 시간 **26.7ms → 6.99ms (73% 단축)**
   * **Trade-off:** 캐시 도입 시 원본 데이터(DB)와 캐시 데이터(Redis) 간의 **정합성 불일치 발생 가능성** 및 Redis 인프라 비용 발생
   * **판단 근거:** '상품 목록'은 다수의 사용자에게 빠르게 노출되어야 하는 **전시 도메인**입니다. 따라서 **미세한 데이터 반영 지연보다는 응답 속도 확보와 DB 보호가 비즈니스적으로 더 중요하다고 판단** (전시 도메인 특성 고려)

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

* **문제 상황:** 서비스 규모 확장을 위해 **Scale-out(다중 서버) 환경 도입 후** 동일 스케줄러의 중복 실행으로 인한 **정산 데이터 중복 적재 및 무결성 훼손** 발생
* **기술적 의사결정:**
   * **대안 비교:** 별도의 배치 서버(Jenkins, Airflow) 구축 vs DB 기반 락(ShedLock) 도입
   * **판단 근거:**
      * 현재 프로젝트 규모에서 관리 포인트가 늘어나는 별도 인프라 구축은 **오버 엔지니어링**이라 판단했습니다.
      * RDBMS는 이미 가용 중인 자원이므로 추가 비용 없이 메타데이터 테이블만으로 락 관리가 가능한 **ShedLock**을 채택하여 경량화된 아키텍처를 유지했습니다.
* **해결 및 결과:** ShedLock 도입을 통해 인스턴스 수와 무관하게 **단일 서버 수행을 보장**하고 데**이터 무결성**을 확보
<br>


### 2. 정산 데이터 적재 성능 최적화 (JPA vs JDBC)

* **테스트 배경**
    * 배치 시스템 고도화 전, 데이터 적재 단계의 병목 지점을 파악하기 위해 10만 건의 데이터로 단위 성능 테스트 진행
* **문제 상황**
    * JPA `saveAll()` 사용 시 10만 건 저장에 약 **20초** 소요
* **원인 분석**
    * MySQL의 `IDENTITY` 전략 사용 시, JPA는 PK 값을 확보하기 위해 Insert 쿼리를 매번 단건으로 실행함.
      * 이로 인해 **Batch Insert 최적화 불가** 확인
* **기술적 의사결정**
    * **Trade-off:** JPA의 편의성(1차 캐시, Dirty Checking) 포기 vs 대용량 처리 속도 확보
    * **판단 근거:** 정산 데이터 저장은 조회/수정이 동반되지 않는 **Write-only** 작업임. 영속성 컨텍스트 관리 비용을 지불하는 것보다, 쿼리를 단일 패킷으로 묶어 보내는 JDBC의 처리 속도가 비즈니스적으로 더 효율적이라 판단
* **검증 결과**
    * `JdbcTemplate`을 활용한 Bulk Insert로 전환하여 처리 시간 **20.2s → 0.67s (약 96% 성능 개선)** 달성
* **Current Status & Roadmap**
    * **Current:** 100만 건 배치 환경에서 **Spring Batch 파티셔닝(병렬 처리)** 을 통해 OOM 방지 및 전체 수행 속도를 **1분대**로 단축
    * **Roadmap:** 파티셔닝 구조에 검증된 JDBC Batch 기술을 결합(`JdbcBatchItemWriter` 도입)하여 **DB I/O 부하를 추가적으로 감소**시킬 계획


--------
<br><br>


## 6. 설계 회고 및 한계 분석

### 1. 배치 처리의 자원 격리 전략과 한계

* **설계 전략:**
     * **Time-Window Scheduling:**
       * 100만 건 이상의 대량 데이터를 처리하는 파티셔닝 로직은 CPU와 Disk I/O를 집중적으로 사용합니다. 이를 고려해 실사용자 트래픽이 가장 적은 **새벽 04:00**에 배치를 실행하여 온라인 서비스의 성능 저하를 방지했습니다.
     * **GridSize 최적화:** DB Connection Pool의 여유분을 계산하여 `GridSize=10`으로 설정하여 자원을 최대한 활용하되 고갈시키지 않도록 제한했습니다.

* **한계 및 리스크:**
     * 현재 구조는 온라인 DB와 배치 처리가 **물리적으로 같은 DB 자원**을 공유합니다.
     * 만약 데이터가 1,000만 건 이상으로 급증하여 배치 수행 시간이 길어질 경우 아침 시간대 실사용자 트래픽과 경합이 발생할 위험이 있습니다.

* **향후 개선안:**
     * **Reader DB 분리:** 배치 작업은 읽기 전용 복제본(Slave DB)에서 수행하여 Master DB의 부하를 제거하는 구조로 개선할 계획입니다.

<br>

### 2. Redis 강결합 구조와 단일 장애 지점 리스크 관리

* **설계 전략:** 재고 정합성을 위해 `Redisson` 분산 락을 도입했습니다. DB 락 대비 가볍고 빠른 처리가 가능하여 트래픽 폭주 상황에서도 안정적인 응답 속도를 확보했습니다.

* **한계:**
     * **단일 장애 지점:** 주문 로직이 Redis에 강하게 의존하고 있어 Redis 서버 장애 시 주문 기능 전체가 마비될 리스크가 있습니다.

* **향후 개선안:**
     * **Fallback 메커니즘 구축:** Redis 장애 시 DB 비관적 락으로 자동 전환되는 Fallback 프로세스 구현하여 성능 Trade-off를 감수하더라도 주문 서비스의 가용성을 확보할 예정입니다.
     * **Redis Cluster:** 단일 노드가 아닌 클러스터 구성을 통해 고가용성을 물리적으로 확보해야 함을 인지했습니다.

<br>

### 3. 데이터 무결성과 성능 사이의 Trade-off (캐싱)

* **설계 전략:** '상품 목록 조회'는 데이터의 실시간성보다 **조회 성능**이 비즈니스 매출에 직결된다고 판단하여 `Look-aside` 캐싱 전략을 적용했습니다.

* **한계:** `@CacheEvict`를 적용했으나 네트워크 지연 등으로 인해 캐시 갱신이 실패 시 DB와 캐시 간의 **데이터 불일치** 발생 가능성이 존재합니다.

* **향후 개선안:** 데이터 불일치 허용 범위를 명확히 정의하고 만료 시간을 짧게 가져가되 **Refresh Ahead** 전략을 혼합하여 정합성과 성능의 균형점을 지속적으로 튜닝할 계획입니다.

--------
<br><br>

## 7. ERD

<img width="2400" height="1582" alt="image" src="https://github.com/user-attachments/assets/176389bd-f7ea-4bd8-a30f-0e08b778ae92" />

* **[ ERD Cloud 링크 바로가기 ](https://www.erdcloud.com/d/B5xBxsPqkP4uwSPt4)**

--------
<br><br>

