package com.example.metasns_api.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {
    private int status;
    private T data;

    public static <T> ApiResponse<T> ok(T data){
        return new ApiResponse<>(HttpStatus.OK.value(), data);
    }
}
