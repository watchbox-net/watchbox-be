package net.watchbox.global.dev.controller;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.record.dto.response.ContentRecordResponse;
import net.watchbox.domain.record.service.ContentRecordQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Hidden
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/dev/records")
@Tag(name = "DevWatchRecord")
public class DevWatchRecordController {
    private final ContentRecordQueryService contentRecordService;

    @GetMapping("/{recordId}")
    public ContentRecordResponse getRecordInfo(
            @PathVariable Long recordId)
    {
        return contentRecordService.getRecordInfo(recordId);
    }
}
