package com.dailytable.dailytable.global.response;

import com.dailytable.dailytable.global.common.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private String errorCode;

    public static <T> ApiResponse<T> success(T data){
        return new ApiResponse<>(true, "OK", data, null);
    }

    public static <T> ApiResponse<T> success(String message, T data){
        return new ApiResponse<>(true, message, data, null);
    }

    public static ApiResponse<?> fail(ErrorCode errorCode){
        return new ApiResponse<>(
                false,
                errorCode.getMessage(),
                null,
                errorCode.name()
        );
    }
}