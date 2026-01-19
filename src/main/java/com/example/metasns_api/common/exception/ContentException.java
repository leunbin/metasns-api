package com.example.metasns_api.common.exception;

import org.springframework.http.HttpStatus;

public class ContentException extends ApiException{

    public ContentException(HttpStatus status, String message){
        super(status, message);
    }
}
