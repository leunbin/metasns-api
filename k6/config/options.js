export const options = {
  scenarios: {
    // 1단계: 캐시 생성 (Warm-up)
    warmup: {
      executor: "per-vu-iterations",
      vus: 1,
      iterations: 1,
      maxDuration: "10s",
    },

    // 2단계: 본격적인 200명 부하 테스트
    post_read_test: {
      executor: "per-vu-iterations",
      vus: 200,
      iterations: 10,
      startTime: "11s",
      maxDuration: "30s",
    },
  },
};