# [ KickSync ] 대용량 트래픽 및 정산 최적화 E-commerce 백엔드 플랫폼

> **핵심 가치**
> * 가혹 인프라 제약(WAS 0.8 vCPU, DB 1.0 vCPU, RAM 1.5GB) 모사 ➔ 소프트웨어 아키텍처 튜닝 기반 시스템 물리적 임계점 및 부하 방어 성능 계측
> * 피크 1,000 TPS 선착순 결제 부하 ➔ Read/Write 서킷 스코프 격리 및 정렬 락 적용으로 평균 응답 지연 6.96초에서 83ms로 단축 및 가용성 100% 확보

> **핵심 성과 요약**
> * 선착순 주문 및 외부 결제 검증 ➔ SpEL ID 정렬 락과 Resilience4j Read/Write 서킷브레이커 격리로 외부 PG 장애 시 평균 지연 6.96초에서 83ms(중앙값 1.09ms)로 단축 및 18.5만 건(평균 627.39 TPS) 수용으로 시스템 가용성 100% 확보
> * 대용량 배치 정산 최적화 ➔ PartnerIdPartitioner 10개 파티셔닝과 순방향 스캔 및 JVM 인메모리 Micro-batch 사전 집계 벌크 연산 결합으로 100만 건 정산 시간 14분 16초에서 1분 9초로 단축과 물리 Disk Write 1.8GB에서 26.9MB로 절감 및 DB CPU 87.43%에서 16.35%로 통제
> * 신규 발매 상품 조회 최적화 ➔ EXPLAIN ANALYZE 커버링 인덱스 순차 스캔과 Redis Look-aside 캐싱 및 Lock-free INCR Rate Limiter 2중 통제망 구축으로 DB CPU 44.95%에서 1.48%로 통제 및 P95 지연 94.4% 단축 기반 처리량 16.6% 확장
> * 사내 DB 보안 AIOps 파이프라인 ➔ Air-gapped 로컬 런타임 Ollama 및 MySQL MCP Server JSON-RPC와 Ralph Loop 자율 디버깅 결합으로 스키마 환각률 0% 통제 및 개발 생산성 30% 확보

<br>

<div align="left">
  <h3>API 테스트 & 문서 Swagger UI</h3>
  
  <a href="http://134.185.116.180/swagger-ui/index.html#/">
  <img src="https://img.shields.io/badge/Swagger_UI-Live_Test-85EA2D?style=for-the-badge&logo=swagger&logoColor=black" alt="Swagger UI" />
</a>
</div>

> Swagger UI 기반 API 직접 호출 테스트 지원. 로그인 후 발급 Access Token Authorize 버튼 입력으로 테스트 가능.

<br><br>

## 1. 프로젝트 소개

**[ KickSync ]** 대규모 트래픽 및 데이터 발생 이커머스(KREAM, StockX) 환경 대상 안정성 및 성능 최적화 중심 백엔드 아키텍처.
플랫폼 성장에 따른 트래픽 및 정산 데이터 증가 ➔ 시스템 확장성 확보 및 데이터 정합성 보장 파이프라인 구축.

### 주요 도메인 기능

* Commerce 주문 및 동시성 제어
    * 다중 락 교착 상태 리스크 ➔ SpEL 기반 상품 ID 오름차순 정렬 락 도입으로 데드락 방어
    * 커밋 전 락 조기 해제에 따른 갱신 손실 리스크 ➔ 독립 물리 트랜잭션 분리 기반 데이터 정합성 확보
    * 외부 PG 조회 장애 전이 리스크 ➔ Read/Write 서킷브레이커 스코프 격리와 Read 회로 0ms Fail-Fast 차단 및 결제 승인 Write 회로 독립 수용으로 연쇄 장애 방어

* Settlement 입점사 100만 건 대용량 정산
    * 멀티스레드 동시 쓰기에 따른 InnoDB 공유 갭 락 충돌 리스크 ➔ 입점사 식별자 10개 파티션 분할 및 비동기 워커 스레드 풀 분배로 갭 락 충돌 배제
    * 누적 스캔 오버헤드 ➔ 커넥션 소켓 유지 기반 단방향 순방향 스캔으로 스캔 부하 통제
    * 단건 동기 실행에 따른 디스크 I/O 과부하 리스크 ➔ JVM 힙 메모리 1차 사전 집계 및 다중 벌크 연산 결합으로 물리 디스크 I/O 98.5% 절감
    * 결함 데이터에 따른 전체 배치 롤백 리스크 ➔ 에러 전용 DLQ 자동 격리로 배치 가동률 확보

* Catalog & Shield 상품 조회 및 트래픽 방어
    * B+Tree 세컨더리 수직 탐색 Random I/O 오버헤드 ➔ EXPLAIN ANALYZE 기반 커버링 인덱스 순차 스캔 전환으로 쿼리 실행 비용 단축
    * 대규모 조회 부하 및 역직렬화 예외 리스크 ➔ Redis Look-aside 캐싱 및 Custom RestPage Wrapper 도입으로 RDBMS SQL Time 0ms 평탄화
    * 악성 트래픽 유입 부하 리스크 ➔ Redis INCR 원자 연산 기반 Lock-free Rate Limiter 배치로 HTTP 429 0ms 즉시 차단

* AIOps & Observability 사내 보안 지능화
    * 사내 DB 스키마 외부 유출 리스크 ➔ 100% 사내 폐쇄망 Air-gapped 로컬 런타임 Ollama 구동으로 보안 통제
    * LLM 스키마 환각 리스크 ➔ MySQL MCP Server JSON-RPC 기반 DB 정형 스키마 메타데이터 주입으로 환각률 0% 통제
    * 자율 디버깅 물리 반영 리스크 ➔ Ralph Loop 자율 디버깅 및 Human-in-the-loop 최종 승인망 결합으로 개발 생산성 30% 확보

### 디렉토리 구조 Feature-driven Architecture

```text
src/main/java/be/kicksync_backend
├── common                      # 전역 공통 인프라 및 횡단 관심사
│   ├── annotation              # 분산 락 및 RateLimit 커스텀 어노테이션
│   ├── aop                     # SpEL 정렬 분산 락 및 Lock-free RateLimit AOP
│   ├── config                  # Redis Redisson Resilience4j ShedLock Swagger 설정
│   ├── dto                     # ApiResponse RestPage 래퍼 공통 응답 규격
│   ├── exception               # GlobalExceptionHandler 및 표준 에러 규격
│   ├── security                # JWT 필터 및 Stateless 인증 인가
│   └── util                    # 독립 물리 트랜잭션 분리 도우미
└── feature                     # 도메인 주도 패키지 비즈니스 로직 응집
    ├── order                   # 선착순 주문 Order Splitting 다중 결제 비즈니스 로직
    ├── payment                 # 외부 PG 연동 및 Read/Write 서킷브레이커 격리 클라이언트
    ├── settlement              # 대용량 정산 파티셔닝 순방향 스캔 벌크 적재
    ├── product                 # 커버링 인덱스 Redis Look-aside 캐싱 상품 조회
    ├── partner                 # 입점사 관리 및 수수료 정책
    └── user                    # 회원 도메인 및 인증 처리
```

<br><br>

## 2. 시스템 전체 아키텍처

<img width="1277" height="1904" alt="image" src="https://github.com/user-attachments/assets/a9f90172-88df-4866-b9a9-4276b33c904b" />

<br><br>

## 3. 기술 스택

| Category | Technology | Reason for Selection |
| --- | --- | --- |
| **Language** | <img src="https://img.shields.io/badge/Java_21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white"> | Virtual Threads 및 ZGC 환경 고부하 I/O 블로킹 최소화 및 힙 메모리 통제 |
| **Framework** | <img src="https://img.shields.io/badge/Spring_Boot_3.5-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white"> <img src="https://img.shields.io/badge/Spring_Batch-6DB33F?style=for-the-badge&logo=spring&logoColor=white"> | 파티셔닝 기반 대용량 병렬 데이터 분산 및 메타데이터 이력 관리 |
| **Database** | <img src="https://img.shields.io/badge/MySQL_8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white"> <img src="https://img.shields.io/badge/Redis_7.0-DC382D?style=for-the-badge&logo=redis&logoColor=white"> | InnoDB 무결성 보장과 분산 락 및 Look-aside 인메모리 고속 캐싱 확보 |
| **Resilience & AI** | <img src="https://img.shields.io/badge/Resilience4j-FF5722?style=for-the-badge&logo=springboot&logoColor=white"> <img src="https://img.shields.io/badge/Ollama-FFFFFF?style=for-the-badge&logo=ollama&logoColor=black"> <img src="https://img.shields.io/badge/MCP-1A1A1A?style=for-the-badge&logo=json&logoColor=white"> | Read/Write 아웃바운드 서킷브레이커 스코프 격리 및 폐쇄망 MCP 스키마 주입 통제 |
| **ORM & Driver** | <img src="https://img.shields.io/badge/Spring_Data_JPA-6DB33F?style=for-the-badge&logo=spring&logoColor=white"> <img src="https://img.shields.io/badge/JDBC-007ACC?style=for-the-badge&logo=java&logoColor=white"> | 도메인 모델링 생산성 확보 및 다중 쿼리 병합 벌크 적재 최적화 |
| **Infra & CI/CD** | <img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white"> <img src="https://img.shields.io/badge/OCI-F80000?style=for-the-badge&logo=oracle&logoColor=white"> <img src="https://img.shields.io/badge/GitHub_Actions-2088FF?style=for-the-badge&logo=github-actions&logoColor=white"> | 단일 노드 격리 배포 자원 제약 모사 기반 아키텍처 임계점 계측 및 무중단 배포 확보 |
| **Test & Monitor** | <img src="https://img.shields.io/badge/k6-7D64FF?style=for-the-badge&logo=k6&logoColor=white"> <img src="https://img.shields.io/badge/Scouter_APM-FF9900?style=for-the-badge&logo=java&logoColor=white"> | 피크 1,000 TPS 부하 인가 및 APM 스레드 메트릭 삼각 계측 통제 |

<br><br>

## 4. 핵심 엔지니어링 최적화 딥다이브

> 하드웨어 증설 없는 소프트웨어 아키텍처 튜닝 기반 물리적 병목 최적화 방어 프로세스

---

### [ Deep-Dive 1 ] 다중 락 교착 및 외부 결제 연쇄 장애 방어

<img width="2034" height="1728" alt="image" src="https://github.com/user-attachments/assets/d4e3ecf3-9f12-484c-b7a1-044dd5712223" />

* **문제 원인**
    * SpEL 다중 락 키 정렬 누락에 따른 스레드 간 교착 상태 ➔ Tomcat 스레드 200개 대기 풀 정체 및 평균 응답 시간 6.96초 지연 병목 식별
    * 락 해제 및 DB 트랜잭션 커밋 시점 불일치 ➔ 트랜잭션 커밋 전 락 조기 해제에 따른 타 스레드 과거 재고 판독 및 초과 판매 데이터 정합성 오류 식별
    * 500ms 외부 결제 API 네트워크 지연 강결합 ➔ 커넥션 10개 전량 고갈 및 후속 사용자 JWT 인증 필터 연쇄 타임아웃 0% 가용성 장애 식별
* **해결 과정**
    * 스레드 간 교착 상태 리스크 ➔ 상품 ID 오름차순 정렬 데드락 방어 및 단일 상품 락 우회 튜닝으로 다중 락 대비 Redis CPU 사용률 4.16%에서 2.15%로 통제
    * 초과 판매 및 커넥션 장기 점유 리스크 ➔ 분산 락 기반 독립 트랜잭션 생명주기 분리 및 동등 탐색 상쇄로 커밋 완료 시점 DB 커넥션 점유 시간 단축
    * 외부 결제 연쇄 장애 리스크 ➔ 결제 연동 로직 트랜잭션 외부 분리 및 Resilience4j Read/Write 서킷브레이커 스코프 격리 적용으로 외부 장애 시 Read 서킷 0ms Fail-Fast 통제
* **정량적 실측 성과**
    * 평균 응답 지연 6.96초에서 83ms(중앙값 1.09ms)로 단축 및 5분간 총 185,692건 트랜잭션 안정 수용으로 처리량 9.6배 확보
    * 대기 스레드 전량 회수 방어 및 WAS CPU 48.13%와 DB CPU 0.56% 통제 기반 자원 포화 평탄화
    * 초과 판매 0건과 주문 재고 오차율 0% 통제 및 서킷브레이커 스코프 격리 기반 시스템 가용성 100% 확보

<br>

### [ Deep-Dive 2 ] 100만 건 정산 페이징 및 I/O 병목 최적화

<img width="1360" height="1672" alt="image" src="https://github.com/user-attachments/assets/2e802141-26eb-4a2a-bec0-ec6732eb8902" />

* **문제 원인**
    * 페이징 후반부 누적 OFFSET 유발 순차 스캔 가중 ➔ 단일 쿼리 실행 774초 소요 단일 스레드 정체 현상 식별
    * 동기식 쓰기 루프 기반 네트워크 왕복 및 Redo 로그 동기화 강결합 ➔ 1.8GB 물리 Disk Write 병목 식별
    * 다중 스레드 동시 쓰기 시도에 따른 배타적 락 획득 대기 ➔ 커넥션 장기 점유 및 DB CPU 87.43% 포화 현상 식별
* **해결 과정**
    * 스레드 간 락 경합 리스크 ➔ 범위 파티셔닝 기반 10개 병렬 워커 스레드 분배 구조로 데드락 가능성 배제
    * 대용량 데이터 적재 I/O 지연 ➔ 커넥션 소켓 유지 순방향 스캔과 JVM 인메모리 Micro-batch 사전 집계로 DML 요청 통제
    * DB 다중 쓰기 패킷 비용 및 결함 데이터 롤백 리스크 ➔ 다중 쓰기 벌크 쿼리 옵션 통합 및 에러 전용 DLQ 자동 격리 아키텍처 통제
* **정량적 실측 성과**
    * 100만 건 정산 처리 시간 14분 16초에서 1분 9초로 단축(12.3배 향상) 및 WAS CPU 연산 전환 확보
    * 물리 Disk Write 부하 1.8GB에서 26.9MB로 98.5% 절감 및 DB CPU 점유율 평균 16.35% 통제
    * 150억 원 정산 금액 정합성 100% 일치 및 결함 에러 격리 기반 가동률 100% 무중단 배치 인프라 통제

<br>

### [ Deep-Dive 3 ] 신규 발매 상품 조회 RDBMS 병목 방어

<img width="1358" height="1497" alt="image" src="https://github.com/user-attachments/assets/a0c3dd7f-fdae-4b27-8ed2-bb9dd3ad3221" />

* **문제 원인**
    * 500만 건 규모 순차 스캔 및 Random I/O 수직 탐색 병목 ➔ 10개 제한 HikariCP DB 커넥션 풀 고갈 및 평균 32ms SQL 처리 지연 식별
    * DB 커넥션 경합에 따른 스레드 대기 상태 정체 ➔ 응답 시간 최장 9초 지연 및 시스템 정체 리스크 식별
    * 요청 정체에 따른 힙 메모리 1,000MB 포화 ➔ 최대 12초 STW 스파이크 및 GC 쓰레싱 기반 가용성 저하 리스크 식별
* **해결 과정**
    * 세컨더리 인덱스 탐색 오버헤드 ➔ EXPLAIN ANALYZE 기반 커버링 인덱스 순차 스캔 전환으로 DB 엔진 레벨 쿼리 실행 오버헤드 단축
    * 데이터 정합성 유지 한계 및 잦은 조회 병목 ➔ 상품 조회 Redis Look-aside 캐시 결합 및 데이터 정합성 강제 무효화 전략 적용으로 조회 성능 확보
    * 악성 트래픽 유입에 따른 DB 커넥션 고갈 리스크 ➔ 서비스 진입점 Redis INCR 원자 연산 기반 Lock-free Rate Limiter 배치로 악성 트래픽 진입 0ms 즉시 차단
* **정량적 실측 성과**
    * 커버링 인덱스 및 Look-aside 캐시 결합으로 DB CPU 점유율 44.95%에서 평균 1.48%로 통제 및 SQL Time 0ms 확보
    * 자원 포화 대기 시간 제외 실질 트랜잭션 응답 속도 최장 9.0초에서 0.5초 이하 대역 안착(94.4% 단축) 및 총 처리량 50,484건에서 58,902건으로 16.6% 확장 확보
    * 134개 커넥션 획득 대기 스레드 및 69개 동기화 락 경합 상태 해소 기반 초과 부하 13.67% HTTP 429 즉시 차단으로 유효 트래픽 방어율 86.33% 확보

<br>

### [ Deep-Dive 4 ] 사내 DB 보안 AIOps 파이프라인 구축

<img width="1544" height="1202" alt="image" src="https://github.com/user-attachments/assets/fb24571a-12fc-4248-b20c-11ee564eeb5a" />

* **문제 원인**
    * 복잡한 엔티티 의존성 해소를 위한 퍼블릭 LLM 도입 한계 ➔ 대화 토큰 누적에 따른 전역 상태 ERD 유실 및 컨텍스트 단절 현상 식별
    * 실시간 변경 데이터베이스 스키마 메타데이터 미인지 ➔ 존재하지 않는 컬럼 쿼리 시도 기반 스키마 환각 오류 식별
    * AI 생성 결함 코드 수동 디버깅 반복 ➔ 코드 리뷰 비용 증가 및 개발 공정 생산성 저하 병목 식별
* **해결 과정**
    * 스키마 컨텍스트 유실 및 보안 리스크 ➔ 폐쇄망 Air-gapped 런타임 적용 및 불변 아키텍처 규칙 영속화 파일 보존으로 하이브리드 컨텍스트망 확보
    * 스키마 환각 및 비정상 DML 오용 리스크 ➔ MySQL MCP 서버 연동 정형 스키마 주입 및 파괴적 쿼리 차단용 가드레일 레이어 통제
    * 수동 디버깅 오버헤드 ➔ 에이전트 간 실패 내역 기반 자동 롤백 및 재작성 수행 자율 디버깅 파이프라인 통제
* **정량적 실측 성과**
    * MySQL MCP 연동 구조 기반 실시간 스키마 파악 및 스키마 환각률 0% 통제
    * 자율 디버깅 파이프라인 기계 오프로딩 적용으로 핵심 비즈니스 로직 고도화 집중 및 전체 생산성 30% 확보
    * 인프라 보안 가드레일과 실시간 알림 통제망 내 최종 승인 절차 결합으로 코딩 컨벤션 강제 AIOps 파이프라인 확보

<br><br>

## 5. 트러블 슈팅 및 설계 회고

### 1. 분산 락 단일 다중 분기 튜닝
단일 상품 락 우회 분기 설계로 다중 락 대비 Redis CPU 점유율 4.16%에서 2.15%로 48.3% 추가 절감

### 2. 분산 환경 스케줄러 중복 실행 방어
RDBMS 메타데이터 기반 ShedLock 채택으로 다중 인스턴스 환경 정산 배치 중복 실행 무결성 확보

### 3. Java 21 APM Attach API 호환 한계 통제
WAS 컨테이너 내부 런타임 접속 및 진단 도구 직접 호출 독립 수집 경로 구축으로 고부하 환경 스레드 메트릭 정밀 분석 통제

<br><br>

## 6. ERD 데이터베이스 모델링

<img width="897" height="755" alt="image" src="https://github.com/user-attachments/assets/795df007-f910-40dd-ab6b-1498c5d6cd0b" />

<br><br>

## 7. 인프라 운영 및 CI/CD 파이프라인

* 컨테이너 물리 자원 제약 모사 기반 프로덕션 부하 임계점 계측 및 격리 환경 배포 통제
* GitHub Actions 연동 빌드 파이프라인 및 OCI 인스턴스 자동 배포 환경 확보
