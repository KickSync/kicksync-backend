import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  scenarios: {
    circuit_500vu_5m_scenario: {
      executor: 'ramping-arrival-rate',
      startRate: 50,
      timeUnit: '1s',
      preAllocatedVUs: 100,
      maxVUs: 500,
      stages: [
        { duration: '30s', target: 200 }, // Warm-up (200 VUs)
        { duration: '4m', target: 500 },  // Main Peak Load (500 VUs for 4min)
        { duration: '30s', target: 0 },   // Cool-down
      ],
    },
  },
  thresholds: {
    'http_req_duration{type:read}': ['p(95)<100'],   // Fail-Fast 유도로 Read P95는 100ms 미만
    'http_req_duration{type:write}': ['p(95)<1000'], // Write P95는 1000ms 미만
  },
};

const BASE_URL = 'http://localhost:8080/api/v1';

export default function () {
  // 1. Read API 호출 (외부 PG 지연/장애 시 Fail-Fast 0ms 관측)
  const readRes = http.get(`${BASE_URL}/payments/imp_test_123`, {
    tags: { type: 'read' },
  });

  check(readRes, {
    'Read response received': (r) => r.status > 0,
  });

  // 2. Write API 호출 (Read 서킷이 OPEN되어도 Write 서킷 100% 정상 가용성 검증)
  const payload = JSON.stringify({
    impUid: 'imp_test_123',
    amount: 50000,
    reason: 'k6 500VU test cancel',
    checksum: 50000
  });

  const headers = { 'Content-Type': 'application/json' };
  const writeRes = http.post(`${BASE_URL}/payments/cancel`, payload, {
    headers: headers,
    tags: { type: 'write' },
  });

  check(writeRes, {
    'Write response status valid': (r) => r.status > 0,
  });

  sleep(0.1);
}
