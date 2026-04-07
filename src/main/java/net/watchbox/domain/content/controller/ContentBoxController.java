package net.watchbox.domain.content.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/contents/{contentId}/boxes")
@Tag(name = "ContentBox", description = "컨텐츠 기준 박스 포함 여부 조회 및 일괄 추가/삭제 API")
public class ContentBoxController {
}
