package net.watchbox.domain.notification.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;
import net.watchbox.domain.notification.dto.payload.NotificationPayload;
import org.springframework.stereotype.Component;

/**
 * NotificationPayload (sealed interface) ↔ JSON 문자열 변환.
 *
 * <p>엔티티에서 사용:
 * <pre>{@code
 *   @Convert(converter = NotificationPayloadConverter.class)
 *   @Column(columnDefinition = "json")
 *   private NotificationPayload payload;
 * }</pre>
 *
 * <p>Jackson 의 @JsonTypeInfo 가 type discriminator 를 자동 처리 →
 * 역직렬화 시 NotificationType 에 맞는 구체 record 로 복원.
 */
@Component
@Converter
@RequiredArgsConstructor
public class NotificationPayloadConverter implements AttributeConverter<NotificationPayload, String> {

    private final ObjectMapper objectMapper;

    @Override
    public String convertToDatabaseColumn(NotificationPayload attribute) {
        if (attribute == null) return null;
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("NotificationPayload 직렬화 실패: " + attribute, e);
        }
    }

    @Override
    public NotificationPayload convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) return null;
        try {
            return objectMapper.readValue(dbData, NotificationPayload.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("NotificationPayload 역직렬화 실패: " + dbData, e);
        }
    }
}
