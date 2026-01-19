package com.example.metasns_api.common.exception;

import org.springframework.http.HttpStatus;

public class PostException extends ApiException{

    public PostException(HttpStatus status, String message){
        super(status, message);
    }
}
