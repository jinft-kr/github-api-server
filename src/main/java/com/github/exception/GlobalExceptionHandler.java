package com.github.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<Map<String, Object>> handleWebClientResponseException(WebClientResponseException e) {
        log.error("GitHub API WebClient 예외 발생", e);

        HttpStatus status = (HttpStatus) e.getStatusCode();
        String message;

        if (status == HttpStatus.UNAUTHORIZED) {
            message = "Unauthorized: 유효하지 않은 인증 정보입니다";
        } else if (status == HttpStatus.NOT_FOUND) {
            message = "해당 정보를 찾을 수 없습니다";
//        } else if (status == HttpStatus.FORBIDDEN && e.getResponseBodyAsString().contains("rate limit")) {
//            status = HttpStatus.TOO_MANY_REQUESTS;
//            message = "GitHub API rate limit 초과";
        } else if (status == HttpStatus.FORBIDDEN) {
            message = "Forbidden: 권한 부족 또는 일시적인 제한";
        } else {
            message = "GitHub API 오류 발생: " + e.getResponseBodyAsString();
        }

        return buildErrorResponse(HttpStatus.OK, message);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatusException(ResponseStatusException e) {
        log.error("GitHub API ResponseStatus 예외 발생", e);
        return buildErrorResponse(HttpStatus.OK, e.getReason() != null ? e.getReason() : "요청 처리 실패");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(Exception e) {
        log.error("서버 에러 발생", e);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다");
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatus status, String message) {
        Map<String, Object> errorBody = Map.of(
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", message,
                "timestamp", OffsetDateTime.now(ZoneOffset.UTC).toString()
        );
        return new ResponseEntity<>(errorBody, status);
    }
}
