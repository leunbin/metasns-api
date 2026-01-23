import http from 'k6/http';
import { check, sleep } from 'k6';
import { uploadHeaders } from "../utils/headers.js";
import { options } from "../config/upload_options.js";

export { options };

const file = open('../test-image.jpg', 'b');

export default function () {
    const url = 'http://host.docker.internal:8080/api/v1/post/1/contents';

    const payload = {
        file: http.file(file, 'test-image.jpg', 'image/jpeg'),
    };

    const res = http.post(url, payload, {
        headers: uploadHeaders(__ENV.TOKEN),
    })

    if (res.status !== 202) {
        console.log(`Response Status: ${res.status}`);
        console.log(`Response Body: ${res.body}`);
    }

    check(res, {
        'status is 200 or 202': (r) => r.status === 200 || r.status === 202,
      });

      sleep(0.1);
}