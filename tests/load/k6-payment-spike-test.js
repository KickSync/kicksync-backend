import http from 'k6/http';
import { check } from 'k6';
import { Trend, Counter } from 'k6/metrics';
import exec from 'k6/execution';

const orderLatency = new Trend('order_latency_ms');
const successCounter = new Counter('successful_orders');
const failureCounter = new Counter('failed_orders');

// Load token_list.txt
const tokens = open('./token_list.txt').split('\n').filter(t => t.trim().length > 0);

export const options = {
  scenarios: {
    concurrency_contention: {
      executor: 'per-vu-iterations',
      vus: 1000,
      iterations: 1,
      maxDuration: '10s',
      startTime: '0s',
      exec: 'runContention',
    },
    payment_spike_protection: {
      executor: 'ramping-arrival-rate',
      startRate: 10,
      timeUnit: '1s',
      preAllocatedVUs: 100,
      maxVUs: 1000,
      stages: [
        { duration: '5s', target: 10 },
        { duration: '1s', target: 1000 },
        { duration: '10s', target: 1000 },
        { duration: '5s', target: 0 },
      ],
      startTime: '15s',
      exec: 'runSpike',
    },
  },
  thresholds: {
    // Scenario-specific thresholds for isolated verification
    'http_req_duration{scenario:concurrency_contention}': ['p(95)<1500'],
    'http_req_duration{scenario:payment_spike_protection}': ['p(95)<1500'],
    
    // Fail if error rate of request failures is extremely high (e.g. over 95%)
    'http_req_failed{scenario:concurrency_contention}': ['rate<0.95'], 
    'http_req_failed{scenario:payment_spike_protection}': ['rate<0.95'], 
  },
};

const BASE_URL = 'http://127.0.0.1:8080';

// Scenario 1 Logic: Contention on hot product (Product ID: 1, Stock: 10)
export function runContention() {
  const vuId = exec.vu.idInTest;
  const token = tokens[vuId - 1];
  if (!token) {
    console.warn(`[WARN] No token found for VU ID ${vuId}`);
    return;
  }

  const payload = JSON.stringify({
    orderItems: [{ productId: 1, quantity: 1 }],
    receiverName: "Contention VU Tester",
    receiverPhone: "010-0000-0000",
    address: { zipcode: "12345", street: "Contention St.", detail: "Room 1" },
    requestMessage: "Scenario 1: High Contention Order"
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    timeout: '5s',
  };

  const res = http.post(`${BASE_URL}/api/orders`, payload, params);
  orderLatency.add(res.timings.duration);

  if (res.status === 201) {
    successCounter.add(1);
  } else {
    failureCounter.add(1);
  }

  const validStatuses = [201, 400, 409];
  check(res, {
    'Status code is 201/400/409': (r) => validStatuses.includes(r.status),
    'Response time < 5s': (r) => r.timings.duration < 5000,
  });
}

// Scenario 2 Logic: Spiking requests to observe Circuit Breaker
export function runSpike() {
  const vuId = exec.vu.idInTest;
  const vuIndex = (vuId - 1) % tokens.length;
  const token = tokens[vuIndex];
  if (!token) {
    console.warn(`[WARN] No token found for VU Index ${vuIndex}`);
    return;
  }

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

  const params = {
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    timeout: '5s',
  };

  const res = http.post(`${BASE_URL}/api/payments/verify`, payload, params);
  orderLatency.add(res.timings.duration);

  if (res.status === 200) {
    successCounter.add(1);
  } else {
    failureCounter.add(1);
  }

  // Expect either success (200), client error (400), or server error/circuit breaker fallback (500)
  const expectedSpikeStatuses = [200, 400, 500];
  check(res, {
    'Status code is expected (200/400/500)': (r) => expectedSpikeStatuses.includes(r.status),
    'Response time < 5s': (r) => r.timings.duration < 5000,
  });
}

// Default export: executes both paths sequentially in dry-runs to validate execution safety
export default function() {
  runContention();
  runSpike();
}
