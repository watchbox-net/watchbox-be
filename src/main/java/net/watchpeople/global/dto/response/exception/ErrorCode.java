package net.watchpeople.global.dto.response.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
