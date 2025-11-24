package net.watchpeople.global.dto.response;

import lombok.*;

@Builder
@Getter
public class ApiResponse<T> {
    private boolean success;
    private T data;
    private ErrorDetail error;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ErrorDetail {
        private String code;        // 에러 코드
        private String detail;      // 상세 설명
//        private String field;       // 에러 발생 필드
    }

    // ===== 성공 응답 생성 메서드 =====
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .error(null)
                .build();
    }

    public static ApiResponse<Void> success() {
        return ApiResponse.<Void>builder()
                .success(true)
                .data(null)
                .error(null)
                .build();
    }

    // ===== 실패 응답 생성 메서드 =====
    public static <T> ApiResponse<T> error(ErrorDetail error) {
        return ApiResponse.<T>builder()
                .success(false)
                .data(null)
                .error(error)
                .build();
    }

}
