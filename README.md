# [ KickSync ] 대용량 트래픽을 감당하는 선착순 한정판 거래 플랫폼

> **핵심 가치**
> * 단순 기능 구현에 머물지 않고 AWS t3.medium(2vCPU/4GB) 제약 환경을 모사하여 아키텍처의 한계를 데이터로 증명
> * 지속적인 아키텍처 고도화를 통해 100만 건 정산 시간을 2시간 23분에서 1.1초로 단축하고 데드락 없이 재고 정합성을 보장하는 고가용성 시스템 구축

> **핵심 성과 요약**
> * **동시성 제어 및 가용성:** MultiLock 정렬 및 Fail-Fast 전략으로 동시 접속 상황 재고 오차율 0% 달성 및 처리량 6배 향상
> * **배치 성능 개선:** 도메인 반정규화 및 DB 레벨 집계와 Spring Batch 파티셔닝을 통해 100만 건 정산 시간 2시간 23분에서 1.1초로 단축
> * **조회 성능 최적화:** Redis Look-aside 캐싱 적용으로 동일 제약 환경 대비 TPS 5.6배 향상(143 → 811) 및 DB CPU 점유율 102.3%에서 0.73%로 안정화

<br>

<div align="left">
  <h3>API 테스트 & 문서 (Swagger UI)</h3>
  
  <a href="http://134.185.116.180/swagger-ui/index.html#/">
  <img src="https://img.shields.io/badge/Swagger_UI-Live_Test-85EA2D?style=for-the-badge&logo=swagger&logoColor=black" alt="Swagger UI" />
</a>
</div>

> 버튼을 통해 API를 직접 호출해 보실 수 있습니다. 로그인 후 발급된 Access Token을 Authorize 버튼에 입력하여 테스트 가능합니다.

<br><br>

## 1. 프로젝트 소개

**[ KickSync ]** 는 대규모 트래픽과 데이터가 발생하는 이커머스 환경(KREAM, StockX 등)에서의 안정성과 성능 최적화에 주력한 백엔드 프로젝트입니다.

플랫폼 성장에 따라 급증하는 트래픽과 정산 데이터를 효율적으로 처리하기 위해, 시스템 확장성 확보와 데이터 정합성 보장을 최우선 엔지니어링 목표로 설정했습니다.

### 주요 기능

* **Commerce (주문 및 동시성 제어):**
    * **동시성 제어:** 선착순 구매 시 발생하는 재고 충돌 및 데드락을 방지하기 위해, Redisson MultiLock 오름차순 정렬과 커스텀 AOP 및 Fail-Fast 기반의 고가용성 제어 컴포넌트 구축
    * **정산 시스템:** 다중 입점사 통합 결제를 위한 Order Splitting 아키텍처 설계

* **Settlement (대규모 배치 정산):**
    * **대용량 배치:** 100만 건 이상의 정산 데이터를 도메인 반정규화와 DB 레벨 집계로 압축하고 Spring Batch Partitioning을 통해 병렬 처리
    * **장애 허용:** 일시적 결함으로 인한 전체 롤백을 막기 위해, Skip 및 Retry 기반의 에러 격리 로직 적용

* **Auth & Security (인증 및 방어막):**
    * **Stateless:** JWT 기반의 인증 구조로 권한을 분리하고 Redis를 활용한 로그아웃 및 토큰 재발급 구현
    * **Rate Limiter:** 악성 트래픽 방어를 위해 Redis INCR 기반의 Lock-free Rate Limiter 배치

* **Architecture (분산 환경 & 통제):**
    * **데이터 일관성:** 다중 인스턴스 환경에서 ShedLock으로 스케줄러 중복 실행 방지
    * **AI 컨텍스트 제어:** 레거시 분리 과정에서 발생하는 휴먼 에러를 줄이기 위해, MEMORY.md 기반의 역할 분할 AI 파이프라인을 구축하여 아키텍처 가드레일 확립


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
| **Language** | <img src="https://img.shields.io/badge/Java_21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white"> | Virtual Threads를 활용한 고부하 환경 스레드 최적화 및 안정적인 생태계 활용 |
| **Framework** | <img src="https://img.shields.io/badge/Spring_Boot_3.5-6DB33F?style=for-the-badge&logo=spring&logoColor=white"> <img src="https://img.shields.io/badge/Spring_Batch-6DB33F?style=for-the-badge&logo=spring&logoColor=white"> | Chunk 지향 처리를 통한 대용량 데이터의 메모리 효율성 확보 및 배치 실패 이력 관리 |
| **Database** | <img src="https://img.shields.io/badge/MySQL_8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white"> <img src="https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white"> | ACID 트랜잭션 데이터 무결성 보장 및 인메모리 고속 캐싱과 Redisson 분산 락 제어 |
| **ORM** | <img src="https://img.shields.io/badge/Spring_Data_JPA-6DB33F?style=for-the-badge&logo=spring&logoColor=white"> <img src="https://img.shields.io/badge/Hibernate-59666C?style=for-the-badge&logo=hibernate&logoColor=white"> | 객체 지향적 도메인 설계와 생산성 확보 및 JdbcTemplate Bulk Insert 혼용 쿼리 최적화 |
| **Infra** | <img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white"> <img src="https://img.shields.io/badge/Oracle_Cloud-F80000?style=for-the-badge&logo=oracle&logoColor=white"> | Docker 리소스 제한을 통한 물리적 인프라 모사 및 OCI 기반 운영 환경 확보 |
| **Test & Monitor** | <img src="https://img.shields.io/badge/k6-7D64FF?style=for-the-badge&logo=k6&logoColor=white"> <img src="https://img.shields.io/badge/Scouter-00C7B7?style=for-the-badge&logo=scouter&logoColor=white"> | k6 가상 유저 스크립팅 기반 임계점 도출 및 APM과 docker Stats 활용 실시간 병목 모니터링 |

<br><br>

## 4. 기술적 고도화

> **"제한된 자원 속에서의 가설 - 검증 - 개선"**: 하드웨어 스펙에 의존하지 않고 AWS t3.medium 급의 격리된 환경에서 소프트웨어 아키텍처 튜닝만으로 병목을 해결한 과정입니다

### [ Phase 1 ] 동시 접속 상황 재고 정합성 및 가용성 확보

**Q. 장바구니 다중 결제 시 발생하는 데드락과 트래픽 폭주로 인한 서버 마비를 어떻게 방어할 것인가?**

* **문제 상황:** k6를 통한 VUser 500명 동시 주문 스트레스 테스트 결과 재고가 초과 판매되는 갱신 손실 발생 및 스레드 간 락 획득 순서가 꼬여 교착 상태 진입으로 p(95) 지연 시간이 15초에 달하는 마비 리스크 식별
* **기술적 의사결정 및 3단계 해결 과정:**
  1. **가시성 틈새 방어 (트랜잭션 격리):** `@Transactional` 커밋 전에 락이 먼저 풀리는 Spring AOP 프록시의 생명주기 불일치를 막고자 커스텀 AOP(`@DistributedLock`) 도입
  2. **MultiLock 오름차순 정렬 (데드락 사전 차단):** 다중 결제 시 SpEL로 파싱한 상품 ID를 오름차순 정렬하여 모든 스레드가 단방향으로 일관되게 락을 획득하도록 강제해 순환 대기 차단
  3. **Fail-Fast 메커니즘 도입 (스레드 풀 보호):** 락 대기 시간을 15초에서 1초로 축소하여 1초 내 락 미획득 시 즉각 409 예외를 반환해 불필요한 Tomcat 스레드 대기를 끊어내고 가용성 향상
* **최종 성과:**
  * 초과 판매 0건으로 데이터 무결성 확보
  * 병목 해소를 통해 처리 대역폭 9,344건에서 59,045건 상승 및 p(95) 응답 시간 15.0초에서 4.49초 방어 완료

<br>

### [ Phase 2 ] 대용량 정산 병목 해결: 100만 건 처리 시간 2h 23m → 1.1s 단축

**Q. 일일 100만 건의 결제 데이터를 제한된 시간 내에 안정적으로 다중 입점사에게 정산할 수 있는가?**

* **문제 상황:** 초기 `JpaPagingItemReader`를 활용한 단일 스레드 방식으로는 100만 건 기준 후반부 `OFFSET` 부하가 누적되어 2시간 23분이 소요
* **원인 분석:** 100만 건의 데이터를 애플리케이션 메모리로 모두 가져오는 물리적 네트워크 I/O 병목 및 복잡한 런타임 연산 비효율성
* **해결 전략:**
  1. **주문 중심 도메인 반정규화:** 정산 주체를 주문으로 분리하여 1:N 결제 정산을 유연하게 수용
  2. **DB 레벨 집계 (Light Aggregated Read):** 런타임 연산을 제거하고 DB 레벨의 집계 쿼리를 적용하여 애플리케이션으로 이관되는 메모리 부하 방지
  3. **Spring Batch 파티셔닝:** `PartnerId` 범위를 10등분하여 10개의 워커 스레드가 I/O를 병렬로 처리하는 아키텍처 구축
* **최종 성과:**
  * 정산 처리 시간 2시간 23분에서 1.1초로 단축
  * 소수의 데이터 결함이 전체 배치를 멈추지 않도록 Skip 및 Retry 로직을 적용하여 운영 레벨의 배치 가용성 확보

<br>

### [ Phase 3 ] 조회 트래픽 병목 해소: TPS 811.5 확보 및 DB CPU 점유율 0.73% 안정화

**Q. 트래픽의 80%가 집중되는 메인 전시 도메인에서, 2vCPU 인프라의 한계를 어떻게 극복할 것인가?**

* **문제 상황:** k6 부하 인가 시 쿼리 파싱 비용 증가로 DB CPU 점유율이 102.3% 임계점에 도달하여 평균 응답 2.4초 지연 발생
* **해결 과정:**
  1. **물리적 임계점 식별:** 복합 인덱스 튜닝을 마쳤음에도 B-Tree 탐색만으로 발생하는 디스크 I/O 한계 파악
  2. **Redis Look-aside 캐싱:** 조회가 빈번한 1페이지 데이터를 인메모리에 캐싱하여 DB I/O 부하 제거 및 CUD 발생 시 `@CacheEvict`로 캐시를 무효화하여 데이터 정합성 유지
* **검증 결과 (Docker 2vCPU / 4GB 환경 모사):**
  * DB CPU 점유율 102.3%에서 0.73%로 감소
  * 최대 처리량 143.1에서 811.5 TPS로 증가 및 평균 응답 시간 2,420ms에서 12.8ms 단축
  
    | **지표** | **AS-IS (Cache OFF)** | **TO-BE (Redis ON)** | **개선 효과** |
    | --- | --- | --- | --- |
    | **최대 처리량 (Avg TPS)** | 143.1 req/s | **811.5 req/s** | 약 **5.6배** 향상 |
    | **평균 응답 시간 (Avg)** | 2,420 ms | **12.8 ms** | 약 **189배** 단축 |
    | **95% 응답 시간 (p95)** | 3,790 ms | **31.9 ms** | 약 **118배** 단축 |
    | **DB CPU 점유율** | 102.3% (임계치) | **0.73% (여유)** | DB 부하 최적화 |
    | **백엔드 가용성** | 99.99% | **99.59%** | 고부하 안정성 확보 |

> <details>
> <summary><strong>[ 증빙 자료 ] k6 부하 테스트 지표 및 docker Stats 비교</strong></summary>
> <div markdown="1">
> <br>
>
> **[ AS-IS ] Cache OFF: DB CPU 102.3% 임계점 도달 및 타임아웃 발생**
> <br>  
> <img width="1412" height="994" alt="image" src="https://github.com/user-attachments/assets/d47eade7-2837-43ca-be87-78103f3754e8" />
>
> <img width="808" height="148" alt="image" src="https://github.com/user-attachments/assets/d17bc604-2b11-4285-a0f5-99484cc8f51e" />
> <br>
>
> **[ TO-BE ] Redis ON: DB CPU 0.73% 안정화 및 TPS 811.5 확보**
> <br>
>
> <img width="1416" height="1054" alt="image" src="https://github.com/user-attachments/assets/bbf49047-18fd-4e23-a1cb-c71541f92e24" />
> <br>
> <img width="804" height="138" alt="image" src="https://github.com/user-attachments/assets/d51b1d32-1acd-4cae-b4fd-f7d8c52b2f06" />
>
> > >
> </div>
> </details>

<br><br>

## 5. 설계 회고 및 인사이트

### 1. Fail-Fast 방식과 대기열(Queue) 아키텍처 트레이드오프 고민
동시성 제어 시 스레드 풀 보호를 위해 1초 내 거절하는 Fail-Fast 방식을 택했습니다. 대기열을 두면 UX 측면에서 유리할 수 있으나, 100개의 재고가 수초 내에 완판되는 한정판 스니커즈 도메인 특성상 큐를 대기시키는 오버헤드보다 즉시 거절하여 서버의 가용성을 방어하는 것이 비즈니스 목적에 더 부합한다고 판단했습니다.

### 2. 대량 데이터 적재 성능 최적화 (Bulk Insert)
JPA `saveAll()` 사용 시 MySQL `IDENTITY` 전략의 한계로 Batch Insert가 작동하지 않아 10만 건 저장에 약 20초가 소요되었습니다. 정산 데이터 저장은 영속성 컨텍스트 관리가 불필요한 Write-only 작업임을 인지하고 JdbcTemplate 기반의 Bulk Insert로 전환하여 처리 시간을 20.2s에서 0.67s로 단축하며 ORM과 SQL의 적절한 혼용의 중요성을 체득했습니다.

### 3. 분산 환경에서의 스케줄러 통제 (ShedLock)
Scale-out 환경 도입 후 동일 스케줄러의 중복 실행 리스크를 방어하기 위해, 무거운 별도 배치 서버 구축 대신 기존 DB 자원을 활용한 ShedLock을 도입했습니다. 이를 통해 인프라 비용 추가 없이 다중 인스턴스 환경에서의 데이터 무결성을 확보했습니다.

<br><br>

## 6. ERD

<img width="2400" height="1582" alt="image" src="https://github.com/user-attachments/assets/176389bd-f7ea-4bd8-a30f-0e08b778ae92" />

* **[ ERD Cloud 링크 바로가기 ](https://www.erdcloud.com/d/B5xBxsPqkP4uwSPt4)**

--------
