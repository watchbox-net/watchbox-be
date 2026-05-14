package net.watchbox.application.preview;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@Validated
@RequestMapping("/api/preview")
@Tag(name = "Preview", description = "샘플 화면 API")
public class PreviewController {
    private final PreviewFacade previewFacade;

    /**
     * Preview 페이지 대상
     * 1. 박스 페이지
     * 2. 박스 컨텐츠 페이지
     * 3. 시청 기록 페이지
     * 4. 마이 페이지
     */
}
