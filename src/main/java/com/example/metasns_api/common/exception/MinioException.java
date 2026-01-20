package com.example.metasns_api.common.exception;

import org.springframework.http.HttpStatus;

public class MinioException extends ApiException {

    public MinioException(HttpStatus status, String message) {
        super(status, message);
    }

    public MinioException(HttpStatus status, String message, Throwable cause) {
        super(status, message, cause);
    }
}
