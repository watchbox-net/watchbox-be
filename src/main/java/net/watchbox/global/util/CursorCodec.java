package net.watchbox.global.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import net.watchbox.global.dto.CursorPayload;
import net.watchbox.global.dto.response.exception.CustomException;
import net.watchbox.global.dto.response.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 커서 페이지네이션용 인코더/디코더.
 * CursorPayload <-> Base64(JSON) 문자열.
 */
@Component
@RequiredArgsConstructor
public class CursorCodec {
    private final ObjectMapper objectMapper;

    public String encode(CursorPayload payload) {
        try {
            byte[] json = objectMapper.writeValueAsBytes(payload);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(json);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INVALID_CURSOR);
        }
    }

    public CursorPayload decode(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        try {
            byte[] json = Base64.getUrlDecoder().decode(cursor.getBytes(StandardCharsets.UTF_8));
            return objectMapper.readValue(json, CursorPayload.class);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INVALID_CURSOR);
        }
    }
}
