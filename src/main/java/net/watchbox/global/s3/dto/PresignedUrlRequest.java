package net.watchbox.global.s3.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "프리사인드 URL 요청 DTO")
public class PresignedUrlRequest {

    @Schema(description = "저장 폴더 (예: images, spots, profiles)", example = "images")
    private final String prefix;

    @Schema(description = "원본 파일명", example = "photo.jpg")
    private final String fileName;
}
