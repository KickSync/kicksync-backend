import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '5s', target: 500 }, // 빠르게 500명 투입
    { duration: '30s', target: 500 }, 
    { duration: '5s', target: 0 },
  ],
};

const token = open("./token.txt").trim();

export default function () {
  const payload = JSON.stringify({
    orderItems: [{ productId: 1, quantity: 1 }, { productId: 2, quantity: 1 }],
    receiverName: "테스터",
    receiverPhone: "010-1234-5678",
    address: { zipcode: "12345", street: "서울시 강남구", detail: "101호" },
    requestMessage: "TO-BE 500VUs 정합성 테스트"
  });

  const params = {
    headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
    timeout: '60s',
  };

  const res = http.post('http://127.0.0.1:8080/api/orders', payload, params);

  check(res, {
    'valid response (201/400/409)': (r) => [201, 400, 409].includes(r.status),
  });

  sleep(0.1); 
}
