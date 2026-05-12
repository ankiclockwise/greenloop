import http from "k6/http";
import { check, sleep } from "k6";

// Testing read-heavy paths: discovery feed and leaderboard
// These represent the most common user action (browsing available listings)
export const options = {
  stages: [
    { duration: "30s", target: 500 },
    { duration: "1m",  target: 500 },
    { duration: "15s", target: 0  },
  ],
  thresholds: {
    http_req_duration: ["p(95)<2000"],  // NFR from 1.4: feed loads within 2s
    http_req_failed:   ["rate<0.01"],
  },
};

const BASE = "http://localhost:8080";

export default function () {
  const feedRes = http.get(`${BASE}/api/listings`);
  check(feedRes, {
    "feed status 200": (r) => r.status === 200,
    "feed response < 2s": (r) => r.timings.duration < 2000,
  });

  sleep(0.5);

  const lbRes = http.get(`${BASE}/api/impact/leaderboard/students?page=1&limit=10`);
  check(lbRes, {
    "leaderboard status 200": (r) => r.status === 200,
    "leaderboard response < 2s": (r) => r.timings.duration < 2000,
  });

  sleep(1);
}
