package net.watchbox.global.file;

import net.watchbox.global.dto.response.exception.CustomException;
import net.watchbox.global.dto.response.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

/**
 * 파일 URL 이 watchbox S3 경로로 시작하는지 검증한다.
 * 위반 시 400 Bad Request (INVALID_IMAGE_URL) 로 응답되도록 CustomException 을 던진다.
 */
public final class FileUrlValidator {
    @Value("${spring.cloud.aws.s3.bucket}")
    private static String bucketName;

    public static final String REQUIRED_PREFIX =
            "https://" + bucketName + ".s3.ap-northeast-2.amazonaws.com/";

    private FileUrlValidator() {}

    public static void validate(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank() || !fileUrl.startsWith(REQUIRED_PREFIX)) {
            throw new CustomException(ErrorCode.INVALID_IMAGE_URL);
        }
    }

    public static void validateAll(List<String> fileUrls) {
        if (fileUrls == null || fileUrls.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_IMAGE_URL);
        }
        for (String url : fileUrls) {
            validate(url);
        }
    }
}
