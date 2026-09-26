# [ KickSync ] 대용량 트래픽 및 정산 최적화 E-commerce 백엔드 플랫폼

> **핵심 가치**
> 
> - 가혹 인프라 제약 WAS 0.8 vCPU DB 1.0 vCPU RAM 1.5GB 환경 모사 ➔ 소프트웨어 아키텍처 튜닝 기반 시스템 물리적 임계점 및 부하 방어 성능 계측
> - 피크 1,000 TPS 선착순 결제 부하 ➔ Read/Write 서킷 격리 및 정렬 락으로 100만 건 정산 배치 14분 16초에서 1분 9초 단축 및 가용성 100.00% 방어

> **핵심 성과 요약**
> 
> - 선착순 주문 및 외부 결제 검증 ➔ SpEL ID 정렬 락과 Resilience4j Read/Write 서킷브레이커 격리로 외부 PG 장애 시 평균 지연 6.96초에서 83ms 중앙값 1.09ms 단축 및 18.5만 건 수용 627.39 TPS 통제로 시스템 가용성 100.00% 방어
> - 대용량 배치 정산 최적화 ➔ PartnerIdPartitioner 10개 범위 파티셔닝과 커서 스트리밍 및 JVM 인메모리 Micro-batch 사전 집계와 벌크 연산 결합으로 100만 건 정산 시간 14분 16초에서 1분 9초 단축 물리 Disk Write 1.8GB에서 26.9MB 98.5% 절감 및 DB CPU 87.43%에서 16.35% 안정화
> - 신규 발매 상품 조회 최적화 ➔ EXPLAIN ANALYZE 커버링 인덱스 Sequential I/O와 Redis Look-aside 캐싱 및 Lock-free INCR Rate Limiter 2중 통제망으로 DB CPU 점유율 44.95%에서 1.48% 통제 96.7% 부하 평탄화 및 SQL Time 0ms 기록 인프라 가용성 86.33% 확보
> - 사내 DB 보안 AIOps 파이프라인 ➔ Air-gapped 로컬 런타임 Ollama 및 MySQL MCP Server Stdio JSON-RPC와 Ralph Loop 자율 디버깅 및 Human Gate 승인망 결합으로 LLM 스키마 환각률 0% 통제 및 개발 생산성 30% 향상

<br>

<div align="left">
  <h3>API 테스트 & 문서 Swagger UI</h3>
  
  <a href="http://134.185.116.180/swagger-ui/index.html#/">
  <img src="https://img.shields.io/badge/Swagger_UI-Live_Test-85EA2D?style=for-the-badge&logo=swagger&logoColor=black" alt="Swagger UI" />
</a>
</div>

> Swagger UI 기반 API 직접 호출 테스트 지원. 로그인 후 발급 Access Token Authorize 버튼 입력 테스트 가능.

<br><br>

## 1. 프로젝트 소개

**[ KickSync ]** 대규모 트래픽 및 데이터 발생 이커머스 환경 KREAM StockX 대상 안정성 및 성능 최적화 주력 엔지니어링 백엔드 프로젝트입니다.

플랫폼 성장 에 따른 트래픽 및 정산 데이터 증가 ➔ 시스템 확장성 확보 및 데이터 정합성 보장 최우선 엔지니어링 목표 설정 통제.

### 주요 도메인 기능

- Commerce 주문 및 동시성 제어
    - 다중 락 교착 상태 Deadlock 리스크 ➔ SpEL 기반 상품 ID 오름차순 정렬 MultiLock 도입으로 데드락 방어
    - 커밋 전 락 사전 해제 에 따른 갱신 손실 Overselling 리스크 ➔ 독립 물리 트랜잭션 분리 AOP 구축으로 데이터 정합성 확보
    - 외부 PG 조회 장애 에 따른 연쇄 장애 리스크 ➔ Read/Write 서킷브레이커 스코프 격리 및 Read 회로 0ms Fail-Fast 차단과 결제 승인 Write 회로 1.09ms 독립 수용으로 연쇄 장애 방어
- Settlement 입점사 100만 건 대용량 정산
    - 멀티스레드 동시 쓰기 에 따른 InnoDB 공유 갭 락 충돌 리스크 ➔ 입점사 식별자 ID 10개 파티션 분할 및 10개 비동기 스레드 풀 분배로 갭 락 충돌 배제
    - LIMIT/OFFSET 페이징 O(N^2) 누적 스캔 오버헤드 ➔ 커넥션 소켓 기반 O(N) 선형 순차 스트리밍 수신으로 스캔 부하 해결
    - 건별 동기 실행 에 따른 디스크 I/O 과부하 리스크 ➔ JVM 힙 메모리 1차 사전 집계 및 Multi-Row Bulk Write 결합으로 물리 디스크 I/O 98.5% 절감
    - 결함 데이터 에 따른 전체 배치 롤백 리스크 ➔ 최대 100회 Skip 허용 및 에러 전용 DLQ 격리로 배치 가용성 확보
- Catalog & Shield 상품 조회 및 트래픽 방어
    - B+Tree 세컨더리 2단 점프 Random Read I/O 오버헤드 ➔ EXPLAIN ANALYZE 기반 커버링 인덱싱 및 순차 탐색 Sequential I/O 구조로 쿼리 비용 감축
    - 대규모 핫데이터 조회 부하 및 역직렬화 예외 리스크 ➔ Redis Look-aside 캐싱 및 Custom RestPage Wrapper 도입으로 RDBMS Avg SQL Time 0ms 평탄화
    - 악성 트래픽 유입 부하 리스크 ➔ Redis INCR 원자 연산 기반 Lock-free Rate Limiter 배치로 HTTP 429 0ms 즉시 차단
- AIOps & Observability 사내 보안 지능화
    - 사내 DB 스키마 외부 유출 리스크 ➔ 100% 사내 폐쇄망 Air-gapped 로컬 런타임 Ollama 구동으로 보안 통제
    - LLM 스키마 환각 리스크 ➔ Anthropic Model Context Protocol Stdio JSON-RPC 기반 DB 정형 스키마 메타데이터 주입으로 환각률 0% 통제
    - 자율 디버깅 물리 반영 리스크 ➔ Ralph Loop 3회 자가 치유 피드백 및 엔지니어 1-Click Human Gate 승인망 결합으로 개발 생산성 30% 향상

### 디렉토리 구조 Feature-driven Architecture

```
src/main/java/be/kicksync_backend
├── common                      # 전역 공통 인프라 및 횡단 관심사
│   ├── annotation              # 분산 락 및 RateLimit 커스텀 어노테이션
│   ├── aop                     # SpEL 정렬 분산 락 및 Lock-free RateLimit AOP
│   ├── config                  # Redis Redisson Resilience4j ShedLock Swagger 설정
│   ├── dto                     # ApiResponse RestPage 래퍼 공통 응답 규격
│   ├── exception               # GlobalExceptionHandler 및 표준 에러 규격
│   ├── security                # JWT 필터 및 Stateless 인증 인가
│   └── util                    # 독립 물리 트랜잭션 분리 도우미 AopForTransaction
└── feature                     # 도메인 주도 패키지 비즈니스 로직 응집
    ├── order                   # 선착순 주문 Order Splitting 다중 결제 비즈니스 로직
    ├── payment                 # 외부 PG 연동 및 Read/Write 서킷브레이커 격리 클라이언트
    ├── settlement              # 대용량 정산 파티셔닝 커서 스트리밍 벌크 적재
    ├── product                 # 커버링 인덱싱 Redis Look-aside 캐싱 상품 조회
    ├── partner                 # 입점사 관리 및 수수료 정책
    └── user                    # 회원 도메인 및 인증 처리
```

<br><br>

## 2. 시스템 전체 아키텍처

<img width="1277" height="1904" alt="image" src="https://github.com/user-attachments/assets/69463fdb-89bc-45b9-a74f-ea6522ede7bf" />

<br><br>

## 3. 기술 스택

| Category | Technology | Reason for Selection |
| --- | --- | --- |
| **Language** | <img src="https://img.shields.io/badge/Java_21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white"> | Virtual Threads 및 ZGC 고부하 I/O 블로킹 최소화 및 힙 메모리 통제 |
| **Framework** | <img src="https://img.shields.io/badge/Spring_Boot_3.5.5-6DB33F?style=for-the-badge&logo=spring&logoColor=white"> <img src="https://img.shields.io/badge/Spring_Batch_5-6DB33F?style=for-the-badge&logo=spring&logoColor=white"> | Chunk 지향 처리 범위 파티셔닝 기반 대용량 병렬 데이터 분산 및 Job Repository 실패 이력 관리 |
| **Database** | <img src="https://img.shields.io/badge/MySQL_8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white"> <img src="https://img.shields.io/badge/Redis_7-DC382D?style=for-the-badge&logo=redis&logoColor=white"> | InnoDB ACID 트랜잭션 무결성 보장 Redisson 분산 락 및 Look-aside 인메모리 고속 캐싱 |
| **Resilience & AI** | <img src="https://img.shields.io/badge/Resilience4j-000000?style=for-the-badge&logo=resilience4j&logoColor=white"> <img src="https://img.shields.io/badge/Ollama_Air_gapped-000000?style=for-the-badge&logo=ollama&logoColor=white"> <img src="https://img.shields.io/badge/Model_Context_Protocol-4B32C3?style=for-the-badge&logo=anthropic&logoColor=white"> | Read/Write 아웃바운드 서킷브레이커 스코프 격리 및 사내 폐쇄망 MCP 스키마 자동 주입 파이프라인 |
| **ORM & Driver** | <img src="https://img.shields.io/badge/Spring_Data_JPA-6DB33F?style=for-the-badge&logo=spring&logoColor=white"> <img src="https://img.shields.io/badge/JdbcTemplate_Bulk-59666C?style=for-the-badge&logo=hibernate&logoColor=white"> | 도메인 모델링 생산성 확보 및 Multi-Row Bulk Write 결합 |
| **Infra & CI/CD** | <img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white"> <img src="https://img.shields.io/badge/Oracle_Cloud-F80000?style=for-the-badge&logo=oracle&logoColor=white"> <img src="https://img.shields.io/badge/GitHub_Actions-2088FF?style=for-the-badge&logo=github-actions&logoColor=white"> | Docker 자원 제약 모사 기반 아키텍처 임계점 계측 및 OCI 기반 무중단 배포 파이프라인 자동화 |
| **Test & Monitor** | <img src="https://img.shields.io/badge/k6-7D64FF?style=for-the-badge&logo=k6&logoColor=white"> <img src="https://img.shields.io/badge/Scouter_APM-00C7B7?style=for-the-badge&logo=scouter&logoColor=white"> <img src="https://img.shields.io/badge/jcmd_Telemetry-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white"> | 피크 1,000 TPS ramping 부하 인가 및 Scouter APM docker stats JDK 21 jcmd 스레드 덤프 삼각 계측 |

<br><br>

## 4. 핵심 엔지니어링 최적화 딥다이브

> 하드웨어 증설 없는 소프트웨어 아키텍처 튜닝 기반 물리적 병목 최적화 방어 프로세스
> 

---

### [ Deep-Dive 1 ] 선착순 주문 및 외부 결제 동시성 최적화

**Q. 다중 상품 결제 시 다중 락 및 500ms 외부 PG 장애 상황 시스템 가용성 확보 전략**

- **문제 상황 AS-IS**
    - SpEL 다중 락 키 정렬 누락 에 따른 다중 스레드 교착 상태 Deadlock 리스크 ➔ 톰캣 스레드 200개 정체 현상 확인
    - 트랜잭션 커밋 전 락 조기 해제 에 따른 갱신 손실 초과 판매 리스크 ➔ 데이터 정합성 훼손 현상 식별
    - 외부 결제 API DB 트랜잭션 강결합 에 따른 커넥션 풀 전면 고갈 리스크 ➔ 인증 필터 조회 타임아웃 연쇄 장애 및 가용성 0.00% 하락
    
  <br>
    <img width="1371" height="1191" alt="image" src="https://github.com/user-attachments/assets/113a20d5-bd93-46f8-bff4-6e64e49f8920" />
    <img width="1682" height="1541" alt="image" src="https://github.com/user-attachments/assets/af73b0f2-4837-4395-a26c-869e9c526962" />

- **해결 전략 및 아키텍처**
    - SpEL ID 정렬 락 ➔ 상품 ID 오름차순 정렬 락 획득 강제로 교착 상태 발생 가능성 배제 및 Redis CPU 4.16%에서 2.15% 추가 절감
    - 트랜잭션 생명주기 분리 ➔ 독립 물리 트랜잭션 비즈니스 메소드 가동 및 커밋 완료 후 언락 실행으로 초과 판매 오류 해결
    - 외부 API 트랜잭션 외부 분리 ➔ 500ms 네트워크 연동 대기 시간 외부 격리로 DB 커넥션 점유 시간 단축 통제
    - Resilience4j Read/Write 서킷브레이커 스코프 격리 ➔ 외부 PG 장애 시 Read 0ms Fail-Fast 차단 및 결제 승인 Write 1.09ms 독립 수용으로 연쇄 장애 방어
- **정량적 실측 성과 5분간 500 VUs 피크 스트레스 계측**
    - 평균 응답 지연 ➔ 6,960ms에서 83.03ms 중앙값 1.09ms P95 513ms 98.8% 단축
    - 총 처리량 ➔ 19,307건에서 185,692건 평균 627.39 TPS 9.6배 향상
    - 자원 부하 점유율 ➔ WAS CPU 48.13% 및 DB CPU 0.56% 통제로 84.1% 부하 평탄화
    - 데이터 정합성 및 가용성 ➔ 초과 판매 0건 오차율 0.00% 및 Error Rate 0.00% 기반 시스템 가용성 100.00% 확보

---

### [ Deep-Dive 2 ] 입점사별 100만 건 대용량 정산 최적화

**Q. 100만 건 대용량 결제 데이터 한정 자원 기반 메모리 누수 및 디스크 I/O 병목 해결 전략**

- **문제 상황 AS-IS**
    - LIMIT/OFFSET 페이징 후반부 접근 에 따른 데이터 누적 스캔 리스크 ➔ O(N^2) 누적 스캔 부하 및 SQL Time 774초 소요 현상 확인
    - 단건 동기 실행 반복 에 따른 물리 디스크 fsync 시스템 콜 과부하 리스크 ➔ 1.8GB Disk Write 과부하 발생
    - 복합 유니크 제약조건 멀티스레드 동시 쓰기 에 따른 공유 갭 락 충돌 리스크 ➔ 커넥션 누수 경보 및 DB CPU 87.43% 포화 현상 식별
    
  <br>
    <img width="1360" height="1672" alt="image" src="https://github.com/user-attachments/assets/9a7e76d0-a643-45f2-a8d3-ab650fae93bf" />

- **해결 전략 및 아키텍처**
    - 범위 기반 PartnerIdPartitioner 파티셔닝 ➔ 파티션 물리 격리 10개 병렬 비동기 스레드 분배로 갭 락 경합 및 데드락 가능성 배제
    - JdbcCursorItemReader 선형 Cursor Streaming ➔ 커넥션 소켓 유지 단건 순차 스트리밍 수신으로 O(N) 선형 스캔 보장
    - JVM 인메모리 Micro-batch 사전 집계 ➔ 청크 데이터 힙 메모리 1차 누적합 집계로 DML 요청 수 99% 삭감 통제
    - JDBC 드라이버 벌크 쿼리 옵션 ➔ 다중 쿼리 Multi-Row INSERT 재작성 송신으로 물리 디스크 쓰기량 1.8GB에서 26.9MB 98.5% 절감
    - Fault Tolerant 및 DLQ 가드레일 ➔ 결함 데이터 식별 시 최대 100회 Skip 허용 및 에러 전용 DLQ 테이블 자동 격리로 전체 롤백 방어
- **정량적 실측 성과 100만 건 정산 벤치마크 계측**
    - 총 소요 시간 ➔ 14분 16.29초에서 1분 9.49초 12.3배 단축
    - DB CPU 점유율 ➔ 최대 87.43%에서 평균 16.35% 71.08%p 안정화
    - WAS CPU 가동률 ➔ 평균 16%에서 평균 80.02% 연산 속도 치환 확보
    - 물리 Disk Write I/O ➔ 1.8GB에서 26.9MB 98.5% 삭감
    - SQL Time ➔ 774,656ms 100만 회에서 104ms 33회 99.9% 삭감
    - 정합성 및 가동률 ➔ 정산 금액 150억 원 정합성 100% 일치 및 배치 가동률 100.00% 통제

---

### [ Deep-Dive 3 ] 신규 발매 상품 조회 최적화

**Q. 인기 상품 발매 시 조회 트래픽 폭증 에 따른 500만 건 테이블 RDBMS 물리 한계 극복 전략**

- **문제 상황 AS-IS**
    - B+Tree 세컨더리 인덱스 스캔 후 PK 클러스터드 인덱스 재탐색 에 따른 Random Read I/O 리스크 ➔ 단일 SQL 평균 32ms 지연 발생
    - 커넥션 풀 반납 실패 에 따른 톰캣 스레드 134개 대기 정체 리스크 ➔ 응답 지연 최장 9.0초 연장 확인
    - 지연 요청 객체 누적 에 따른 ZGC STW 스파이크 리스크 ➔ 힙 메모리 1,000MB 도달 및 가용성 82.88% 붕괴 현상 식별
    
    <br>
      <img width="1358" height="1407" alt="image" src="https://github.com/user-attachments/assets/d1e06708-49c6-4377-b4b8-574a50ce131a" />


- **해결 전략 및 아키텍처**
    - EXPLAIN ANALYZE 기반 커버링 인덱싱 ➔ Select절 컬럼 복합 인덱스 100% 포함 및 순차 탐색 Sequential I/O 구조로 쿼리 비용 감축
    - Look-aside 캐싱 및 Custom RestPage Wrapper ➔ 메인 조회 Redis 캐시 우회 및 직렬화 오류 해결로 RDBMS Avg SQL Time 0ms 평탄화
    - Lock-free Rate Limiter ➔ 서비스 진입점 Redis INCR 원자 연산 제어 배치로 악성 트래픽 HTTP 429 0ms 즉시 차단
    - Java 21 jcmd 인라인 도구 계측 ➔ WAS 컨테이너 내부 jcmd 및 jstack 직접 호출 우회 경로 구축으로 락 메트릭 누수 수집 방어
- **정량적 실측 성과 500 VUs 피크 1,000 TPS 부하 계측**
    - DB CPU 점유율 ➔ 44.95%에서 평균 1.48% 최대 24.11% 통제로 96.7% 부하 평탄화
    - RDBMS Avg SQL Time ➔ 32ms에서 0ms Flatline DB 쿼리 부하 소거
    - 총 처리량 ➔ 50,484건에서 58,902건 16.7% 향상
    - 응답 대역 ➔ 최장 9.0s에서 0.50초 이하 띠 형성 94.4% 실질 단축
    - 스레드 락 메트릭 ➔ 대기 스레드 134개에서 0개 및 동기화 락 경합 69개에서 0개 완전 해소
    - 인프라 가용성 ➔ 82.88%에서 86.33% HTTP 429 0ms 방어
 
<br><br>

## 5. 트러블 슈팅 및 설계 회고

### 1. SpEL 파싱 기반 정렬 락 단일 다중 분기 튜닝

- 다중 상품 주문 데드락 방어 목적 MultiLock 적용 시 단일 상품 주문 호출 에 따른 Redis CPU 오버헤드 증가 리스크 ➔ 단일 RLock 우회 분기 설계로 Redis CPU 점유율 4.16%에서 2.15% 48.3% 추가 절감

### 2. 분산 환경 스케줄러 중복 실행 방지

- 다중 인스턴스 Scale-out 환경 동일 정산 배치 중복 실행 에 따른 레이스 컨디션 리스크 ➔ RDBMS 메타데이터 기반 ShedLock 채택으로 추가 인프라 비용 없이 단일 리더 실행 무결성 확보

### 3. Java 21 Scouter APM Attach API 호환 한계 극복

- Java 21 가상 스레드 환경 JDK 9 모듈화 제약 에 따른 APM 스레드 덤프 추출 예외 발생 한계 ➔ WAS 컨테이너 내부 런타임 접속 및 JDK 21 표준 진단 도구 jcmd jstack 직접 호출 독립 수집 경로 구축으로 고부하 환경 스레드 락 메트릭 정밀 분석 통제

<br><br>

## 6. ERD 데이터베이스 모델링

<img width="897" height="755" alt="image" src="https://github.com/user-attachments/assets/795df007-f910-40dd-ab6b-1498c5d6cd0b" />

<br><br>

## 7. 인프라 운영 및 CI/CD 파이프라인

- 컨테이너 가상화 ➔ Docker Compose 기반 WAS MySQL Redis 단일 노드 격리 배포 및 물리 자원 엄격 제한으로 프로덕션 부하 임계점 모사 통제
- 배포 자동화 ➔ GitHub Actions 연동 main 브랜치 주입 시 빌드 및 OCI 인스턴스 자동 배포 파이프라인 확보
