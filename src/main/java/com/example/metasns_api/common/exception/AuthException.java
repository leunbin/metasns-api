package com.example.metasns_api.common.exception;

import org.springframework.http.HttpStatus;

public class AuthException extends ApiException{

    public AuthException(HttpStatus status, String message){
        super(status, message);
    }
}
