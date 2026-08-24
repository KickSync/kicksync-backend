import http from 'k6/http';
import { check } from 'k6';
import { Trend, Counter } from 'k6/metrics';

// 커스텀 계측 지표 정의
const productLatency = new Trend('product_api_latency_ms');
const successCounter = new Counter('successful_requests');
const rateLimitedCounter = new Counter('rate_limited_requests'); // HTTP 429
const errorCounter = new Counter('error_requests');

export const options = {
  scenarios: {
    product_query_stress: {
      executor: 'ramping-arrival-rate',
      startRate: 100,                     // 초당 100 요청으로 시작 (100 TPS)
      timeUnit: '1s',
      preAllocatedVUs: 100,               // 초기 할당 VU
      maxVUs: 500,                        // 최대 할당 VU (500명 독립 사용자)
      stages: [
        { target: 200, duration: '30s' },  // 30초 동안 200 TPS로 Ramp-up (기초 가동)
        { target: 600, duration: '30s' },  // 다음 30초 동안 600 TPS로 Ramp-up
        { target: 1000, duration: '60s' }, // 다음 1분 동안 피크 1,000 TPS 돌파 (Spike)
        { target: 1000, duration: '120s' },// 2분간 피크 1,000 TPS 유지하며 ZGC 및 커넥션 안정성 검증
        { target: 0, duration: '30s' },    // 30초 동안 Ramp-down 종료
      ],
    },
  },
  thresholds: {
    product_api_latency_ms: ['p(95)<500'], // 95% 응답 속도가 500ms 미만이어야 함
    'checks': ['rate>0.95'], // 가용성 검증
  },
  setupTimeout: '60s',
};

const BASE_URL = 'http://127.0.0.1:8080';

export default function () {
  const rand = Math.random();
  let url = '';

  // 현실적인 조회 트래픽 분포 설계
  if (rand < 0.70) {
    // 70% 트래픽: 인기(Hot) 상품 상세 조회 (ID 1 ~ 50)
    const hotProductId = Math.floor(Math.random() * 50) + 1;
    url = `${BASE_URL}/api/products/${hotProductId}`;
  } else if (rand < 0.90) {
    // 20% 트래픽: 일반 상품 상세 조회 (ID 51 ~ 1000)
    const normalProductId = Math.floor(Math.random() * 950) + 51;
    url = `${BASE_URL}/api/products/${normalProductId}`;
  } else {
    // 10% 트래픽: 메인 화면 전체 상품 리스트 조회 (페이지네이션)
    const pageNum = Math.floor(Math.random() * 5); // 0 ~ 4 페이지
    url = `${BASE_URL}/api/products?page=${pageNum}&size=20&sort=id,desc`;
  }

  // 각 VU 별로 가상의 고유 IP 주소 부여 (X-Forwarded-For 헤더 활용)
  // __VU는 1부터 시작하며, 이를 통해 500개의 독립적인 사용자 단말 시뮬레이션
  const clientIp = `192.168.1.${__VU}`;

  const params = {
    headers: {
      'Content-Type': 'application/json',
      'X-Forwarded-For': clientIp,
      'X-Test-Purpose': 'Product-Query-Stress-Test'
    },
    timeout: '5s'
  };

  const startTime = new Date();
  const res = http.get(url, params);
  const endTime = new Date();
  const latency = endTime - startTime;
  productLatency.add(latency);

  // 결과 처리 및 응답 코드 모니터링
  if (res.status === 200) {
    successCounter.add(1);
  } else if (res.status === 429) {
    rateLimitedCounter.add(1);
  } else {
    errorCounter.add(1);
    console.error(`[ERROR] status=${res.status}, body=${res.body ? res.body.substring(0, 100) : ''}`);
  }

  check(res, {
    'is 200 or 429': (r) => r.status === 200 || r.status === 429,
  });
}
