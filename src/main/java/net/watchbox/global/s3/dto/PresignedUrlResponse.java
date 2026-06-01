package net.watchbox.global.s3.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "프리사인드 URL 응답 DTO")
public class PresignedUrlResponse {

    @Schema(description = "S3 업로드용 프리사인드 URL (PUT 요청에 사용)", example = "https://ghostrip-bucket.s3.ap-northeast-2.amazonaws.com/images/...")
    private final String presignedUrl;

    @Schema(description = "업로드 후 실제 이미지 접근 URL", example = "https://ghostrip-bucket.s3.ap-northeast-2.amazonaws.com/images/...")
    private final String imageUrl;
}
