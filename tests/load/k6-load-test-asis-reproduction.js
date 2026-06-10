import http from 'k6/http';
import { check, group, sleep } from 'k6';
import { Trend, Counter, Rate } from 'k6/metrics';

const orderLatency = new Trend('order_latency_ms');
const successCounter = new Counter('successful_orders');
const failureCounter = new Counter('failed_orders');
const lockTimeoutRate = new Rate('lock_timeout_errors');
const insufficientStockRate = new Rate('insufficient_stock_errors');
const paymentLatency = new Trend('payment_latency_ms');
const paymentSuccessCounter = new Counter('successful_payments');
const paymentFailureCounter = new Counter('failed_payments');

export const options = {
  stages: [
    { duration: '30s', target: 500 },   // 30초에 걸쳐 500 VUs 투입
    { duration: '240s', target: 500 },  // 240초(4분)간 유지
    { duration: '30s', target: 0 },     // 30초에 걸쳐 정상 종료
  ],
  thresholds: {
    http_req_duration: ['p(95)<3000'],      // 95% 응답 3초 이내로 조정
    http_req_failed: ['rate<0.5'],          // 결제 실패율 감안하여 조정
  },
  setupTimeout: '60s',
};

const BASE_URL = 'http://127.0.0.1:8080';

let token = '';
try {
  token = open("./token.txt").trim();
  if (!token || token.length < 10) {
    console.error('❌ 토큰이 유효하지 않습니다!');
  } else {
    console.log(`✅ 토큰 로드 성공 (길이: ${token.length})`);
  }
} catch (e) {
  console.error(`❌ 토큰 파일 읽기 실패: ${e.message}`);
}

export default function () {
  if (!token || token.length < 10) {
    console.error(`❌ 유효하지 않은 토큰으로 요청 불가`);
    return;
  }

  const params = {
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`,
    },
    timeout: '10s',
  };

  // 50% 주문 생성, 50% 결제 검증 호출
  if (Math.random() < 0.5) {
    // 1. 주문 생성 테스트 (데드락 방지 여부 검증)
    // VU ID가 짝수면 2,1 순서, 홀수면 1,2 순서로 요청하여 데드락 유도 (AS-IS 시 교착상태 발생)
    const orderItems = __VU % 2 === 0
      ? [ { productId: 2, quantity: 1 }, { productId: 1, quantity: 1 } ]
      : [ { productId: 1, quantity: 1 }, { productId: 2, quantity: 1 } ];

    const payload = JSON.stringify({
      orderItems: orderItems,
      receiverName: "K6 테스터",
      receiverPhone: "010-9999-9999",
      address: {
        zipcode: "12345",
        street: "부하테스트 거리",
        detail: "동시성 테스트실"
      },
      requestMessage: "[부하 테스트] 주문 생성 테스트"
    });

    group('Order Creation Concurrency Test', function () {
      const startTime = new Date();
      const res = http.post(`${BASE_URL}/api/orders`, payload, params);
      const endTime = new Date();
      const latency = endTime - startTime;
      orderLatency.add(latency);

      const isSuccess = res.status === 201;
      const isLockTimeout = res.status === 408 || res.body?.includes?.('LOCK_ACQUISITION_FAILED');
      const isInsufficientStock = res.status === 400 || res.body?.includes?.('INSUFFICIENT_STOCK');

      if (isSuccess) {
        successCounter.add(1);
      } else {
        failureCounter.add(1);
        if (isLockTimeout) {
          lockTimeoutRate.add(1);
        } else if (isInsufficientStock) {
          insufficientStockRate.add(1);
        }
      }

      const validStatuses = [201, 400, 409, 408];
      check(res, {
        '상태 코드가 201/400/409/408 중 하나': (r) => validStatuses.includes(r.status),
      });
    });

  } else {
    // 2. 결제 검증 테스트 (Resilience4j CB 및 Retry 검증)
    const payload = JSON.stringify({
      orderId: 1,
      imp_uid: `imp_${Math.floor(Math.random() * 1000000)}`,
      pay_method: "card",
      merchant_uid: `ORD-MOCK-${Math.floor(Math.random() * 1000000)}`,
      paid_amount: 15000,
      pg_provider: "kcp",
      pg_type: "payment",
      pg_tid: `t_${Math.floor(Math.random() * 1000000)}`,
      status: "paid",
      card_name: "신한카드",
      card_number: "1234-5678-****-****"
    });

    group('Payment Verification Resilience Test', function () {
      const startTime = new Date();
      const res = http.post(`${BASE_URL}/api/payments/verify`, payload, params);
      const endTime = new Date();
      const latency = endTime - startTime;
      paymentLatency.add(latency);

      const isSuccess = res.status === 200;
      if (isSuccess) {
        paymentSuccessCounter.add(1);
      } else {
        paymentFailureCounter.add(1);
      }

      // 결제 검증 실패가 예상되나 서버가 뻗지 않고 정상 실패 코드(400 등)를 리턴하는지 확인
      check(res, {
        '상태 코드가 200/400/500 중 하나': (r) => [200, 400, 500].includes(r.status),
      });
    });
  }

  sleep(0.1);
}