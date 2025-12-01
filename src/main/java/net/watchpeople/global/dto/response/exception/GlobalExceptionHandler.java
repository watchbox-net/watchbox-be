package net.watchpeople.global.dto.response.exception;

import lombok.extern.slf4j.Slf4j;
import net.watchpeople.global.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    // 커스텀 예외 처리 핸들러
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException e) {
        log.info(e.getMessage(), e);
        ApiResponse<Void> response = ApiResponse.error(
                ErrorDetail.builder()
                        .code(e.getCode())
                        .message(e.getMessage())
                        .build()
        );
        return ResponseEntity.status(e.getHttpStatus()).body(response);
    }

    // 존재하지 않는 API 경로 요청 처리 핸들러
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoHandlerFoundException(NoHandlerFoundException e) {
        log.info(e.getMessage(), e);
        ApiResponse<Void> response = ApiResponse.error(
                ErrorDetail.builder()
                        .code("API-404")
                        .message("존재하지 않는 API 경로입니다.")
                        .build()
        );
        return ResponseEntity.status(404).body(response);
    }

    // 그외 모든 예외 처리 핸들러
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.info(e.getMessage(), e);
        ApiResponse<Void> response = ApiResponse.error(
                ErrorDetail.builder()
                        .code("SERVER-500")
                        .message("서버 내부 오류가 발생했습니다.")
                        .build()
        );
        return ResponseEntity.status(500).body(response);
    }
}
