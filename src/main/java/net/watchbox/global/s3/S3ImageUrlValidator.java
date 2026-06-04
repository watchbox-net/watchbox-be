package net.watchbox.global.s3;

import net.watchbox.global.dto.response.exception.CustomException;
import net.watchbox.global.dto.response.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

/**
 * 이미지 URL이 watchbox S3 경로로 시작하는지 검증한다.
 * 위반 시 400 Bad Request (INVALID_IMAGE_URL) 로 응답되도록 CustomException 을 던진다.
 */
public final class S3ImageUrlValidator {
    @Value("${spring.cloud.aws.s3.bucket}")
    private static String bucketName;

    public static final String REQUIRED_PREFIX =
            "https://" + bucketName + ".s3.ap-northeast-2.amazonaws.com/";

    private S3ImageUrlValidator() {}

    public static void validate(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank() || !imageUrl.startsWith(REQUIRED_PREFIX)) {
            throw new CustomException(ErrorCode.INVALID_IMAGE_URL);
        }
    }

    public static void validateAll(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_IMAGE_URL);
        }
        for (String url : imageUrls) {
            validate(url);
        }
    }
}
