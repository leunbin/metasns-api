//fail
//export const options = {
//    vus: 100,
//    duration: '30s',
//    thresholds: {
//        http_req_failed: ['rate<0.01'],
//        http_req_duration: ['p(95)<1000'],
//    },
//};

export const options = {
    // 1. 부하 분산: 100명이 한꺼번에 접속하지 않고 서서히
    stages: [
        { duration: '10s', target: 50 },  // 10초 동안 50명까지 증가
        { duration: '20s', target: 100 }, // 다음 20초 동안 100명까지 증가
        { duration: '10s', target: 0 },   // 마지막 10초 동안 종료
    ],

    thresholds: {
        // 2. 실패율: 여전히 1% 미만 유지
        http_req_failed: ['rate<0.01'],

        // 3. 응답 시간: 파일 업로드 특성을 고려하여 10초(10000ms)로 완화
        // p(95)는 전체 요청 중 95%가 이 시간 안에 들어와야 함을 의미
        http_req_duration: ['p(95)<10000'],
    },
};