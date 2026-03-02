package com.dailytable.dailytable.global.handler;

import com.dailytable.dailytable.global.exception.BaseException;
import com.dailytable.dailytable.global.response.ApiResponse;
import org.apache.ibatis.builder.BuilderException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BuilderException.class)
    public ResponseEntity<ApiResponse<?>> handleBusiness(BaseException e){

        return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(ApiResponse.fail(e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleUnknown(Exception e){
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail("SYSTEM ERROR"));
    }
}