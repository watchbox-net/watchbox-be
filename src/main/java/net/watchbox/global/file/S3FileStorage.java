package net.watchbox.global.file;

import lombok.RequiredArgsConstructor;
import net.watchbox.global.file.dto.PresignedUrlResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.UUID;

/**
 * {@link FileService} 의 S3 구현체. AWS S3 의존을 이 클래스 한 곳에 격리한다.
 */
@Service
@RequiredArgsConstructor
public class S3FileStorage implements FileService {

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${spring.cloud.aws.s3.exp-time}")
    private Long expTime;

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    private static final String BASE_URL = "https://%s.s3.ap-northeast-2.amazonaws.com/%s";

    @Override
    public PresignedUrlResponse getUploadPresignedUrl(String prefix, String fileName) {
        String key = createPath(prefix, fileName);

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(
                PutObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMillis(expTime))
                        .putObjectRequest(r -> r.bucket(bucket).key(key))
                        .build()
        );

        String presignedUrl = presignedRequest.url().toString();
        String fileUrl = BASE_URL.formatted(bucket, key);
        return new PresignedUrlResponse(presignedUrl, fileUrl);
    }

    @Override
    public String uploadBytes(String key, byte[] data, String contentType) {
        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType(contentType)
                        .build(),
                RequestBody.fromBytes(data)
        );
        return BASE_URL.formatted(bucket, key);
    }

    @Override
    public void delete(String fileUrl) {
        String key = fileUrl.substring(fileUrl.indexOf(".amazonaws.com/") + ".amazonaws.com/".length());
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build());
    }

    private String createPath(String prefix, String fileName) {
        String fileId = UUID.randomUUID().toString();
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        return "%s/%s-%s-%s".formatted(prefix, timestamp, fileId, fileName);
    }
}
