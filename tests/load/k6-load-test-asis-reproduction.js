import http from 'k6/http';
import { check, group, sleep } from 'k6';
import { Trend, Counter, Rate } from 'k6/metrics';

const orderLatency = new Trend('order_latency_ms');
const successCounter = new Counter('successful_orders');
const failureCounter = new Counter('failed_orders');
const lockTimeoutRate = new Rate('lock_timeout_errors');
const insufficientStockRate = new Rate('insufficient_stock_errors');

export const options = {
  stages: [
    { duration: '30s', target: 500 },   // 30초에 걸쳐 500 VUs 투입
    { duration: '240s', target: 500 },  // 240초(4분)간 유지
    { duration: '30s', target: 0 },     // 30초에 걸쳐 정상 종료
  ],
  thresholds: {
    http_req_duration: ['p(95)<3000'],      // 95% 응답 3초 이내로 조정
    http_req_failed: ['rate<0.2'],          // 실패율 20% 이하로 조정
    'lock_timeout_errors': ['rate<0.05'],   // 락 타임아웃 5% 이하로 조정
  },
  setupTimeout: '60s',
};

const BASE_URL = 'http://127.0.0.1:8080';

let token = '';
try {
  token = open("./token.txt").trim();
  if (!token || token.length < 10) {
    console.error('❌ 토큰이 유효하지 않습니다!');
    console.error(`   토큰 길이: ${token.length}`);
    console.error(`   토큰 내용: ${token.substring(0, 50)}...`);
  } else {
    console.log(`✅ 토큰 로드 성공 (길이: ${token.length})`);
  }
} catch (e) {
  console.error(`❌ 토큰 파일 읽기 실패: ${e.message}`);
}

export default function () {
  if (!token || token.length < 10) {
    console.error(`❌ 유효하지 않은 토큰으로 요청 불가 (토큰 길이: ${token.length})`);
    return;
  }

  const payload = JSON.stringify({
    orderItems: [
      { productId: 1, quantity: 1 },
      { productId: 2, quantity: 1 }
    ],
    receiverName: "K6 테스터",
    receiverPhone: "010-9999-9999",
    address: {
      zipcode: "12345",
      street: "부하테스트 거리",
      detail: "동시성 테스트실"
    },
    requestMessage: "[TO-BE 동시성 최적화] 고강도 부하 테스트"
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`,
      'X-Test-Purpose': 'TO-BE-Concurrency-Optimization'
    },
    timeout: '10s',
  };

  group('TO-BE Concurrency Optimization Test', function () {
    const startTime = new Date();

    const res = http.post(`${BASE_URL}/api/orders`, payload, params);

    const endTime = new Date();
    const latency = endTime - startTime;
    orderLatency.add(latency);

    // 상태 코드별 분류
    const isSuccess = res.status === 201;
    const isLockTimeout = res.status === 408 || res.body?.includes?.('LOCK_ACQUISITION_FAILED');
    const isInsufficientStock = res.status === 400 || res.body?.includes?.('INSUFFICIENT_STOCK');

    if (isSuccess) {
      successCounter.add(1);
      console.log(`✓ 주문 성공 [${latency}ms] - userId=${__VU}`);
    } else {
      failureCounter.add(1);

      if (isLockTimeout) {
        lockTimeoutRate.add(1);
        console.log(`✗ 락 타임아웃 [${latency}ms] - userId=${__VU}`);
      } else if (isInsufficientStock) {
        insufficientStockRate.add(1);
        console.log(`✗ 재고 부족 [${latency}ms] - userId=${__VU}`);
      } else {
        console.log(`✗ 기타 오류 (status=${res.status}) [${latency}ms] - userId=${__VU}`);
      }
    }

    const validStatuses = [201, 400, 409, 408];
    check(res, {
      '상태 코드가 201/400/409/408 중 하나': (r) => validStatuses.includes(r.status),
      'JSON 응답': (r) => r.headers['content-type'] && r.headers['content-type'].includes('application/json'),
      '응답 시간 < 60초': (r) => r.timings.duration < 60000,
    });

    sleep(0.1);
  });
}