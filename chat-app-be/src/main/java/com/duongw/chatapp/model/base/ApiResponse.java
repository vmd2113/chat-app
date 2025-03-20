package com.duongw.chatapp.model.base;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.Instant;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)

public class ApiResponse<T> {

    private Status status;
    private T data;

    @Builder.Default
    private Instant timestamp = Instant.now();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Status {
        private String code;
        private String message;
    }

    public static <T> ApiResponse<T> success(T data) {
        Status status = Status.builder()
                .code(String.valueOf(HttpStatus.OK.value()))
                .message(HttpStatus.OK.getReasonPhrase()).build();

        return ApiResponse.<T>builder()
                .status(status)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> created(T data) {
        Status status = Status.builder()
                .code(String.valueOf(HttpStatus.CREATED.value()))
                .message(HttpStatus.CREATED.getReasonPhrase())
                .build();
        return ApiResponse.<T>builder()
                .status(status)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        Status status = Status.builder()
                .code(code)
                .message(message)
                .build();
        return ApiResponse.<T>builder()
                .status(status)
                .build();
    }

    public static ApiResponse<Void> noContent() {
        Status status = Status.builder()
                .code(String.valueOf(HttpStatus.NO_CONTENT.value()))
                .message(HttpStatus.NO_CONTENT.getReasonPhrase())
                .build();
        return ApiResponse.<Void>builder()
                .status(status)
                .build();
    }

}
