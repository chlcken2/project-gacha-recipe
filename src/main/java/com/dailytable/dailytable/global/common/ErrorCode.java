package com.dailytable.dailytable.global.common;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    USER_NOT_FOUND(HttpStatus.SC_NOT_FOUND, "사용자를 찾을 수 없습니다"),
    INVALID_REQUEST(HttpStatus.SC_BAD_REQUEST, "잘못된 요청입니다"),
    SERVER_ERROR(HttpStatus.SC_INTERNAL_SERVER_ERROR, "서버 오류");

    private final Integer status;
    private final String message;
}
