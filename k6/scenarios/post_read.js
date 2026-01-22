import http from "k6/http";
import { check } from "k6";
import { jsonHeaders } from "../utils/headers.js";
import { options } from "../config/options.js";

export { options };

export default function () {
  const res = http.get(
    "http://host.docker.internal:8080/api/v1/posts/1",
    jsonHeaders(__ENV.TOKEN)
  );

  check(res, { "status is 200": (r) => r.status === 200 });
}
