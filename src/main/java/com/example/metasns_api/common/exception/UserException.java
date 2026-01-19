package com.example.metasns_api.common.exception;

import org.springframework.http.HttpStatus;

public class UserException extends AuthException{

    public UserException(HttpStatus status, String message){
        super(status,message);
    }
}
