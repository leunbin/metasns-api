package com.example.metasns_api.common.exception;

import org.springframework.http.HttpStatus;

public class LikeException extends ApiException{
    public LikeException(HttpStatus status, String message){
        super(status, message);
    }
}
