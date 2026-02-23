package net.watchbox.domain.record.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.watchbox.domain.record.facade.WatchRecordFacade;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/records")
@Tag(name = "WatchRecord", description = "컨텐츠에 대한 시청 상태, 좋아요 API")
public class WatchRecordController {
    private final WatchRecordFacade watchRecordFacade;
    /**
     # 시청 상태
     시청 상태 등록한 리스트 조회 @GetMapping("/status")
     시청 상태 추가 @PostMapping("/status") body: { contentId, mediaType, watchStatus }
     시청 상태 변경 @PatchMapping("/status/{recordId}")
     시청 상태 삭제 @DeleteMapping("/status/{recordId}")

     # 좋아요
     좋아요 등록한 리스트 조회 @GetMapping("/likes")
     좋아요 추가 @PostMapping("/likes") body: { contentId, mediaType, watchStatus }
     좋아요 삭제 @DeleteMapping("/likes/{recordId}")

     # 시청 상태 조회
     시청 기록 단일 조회 @GetMapping("/{recordId}") -> DevController

     ! 시청 상태 삭제 & 좋아요 기록 없음 -> 시청 기록 삭제
     ! 둘 중 어떤 기록을 남기든 DB에 해당 Content 없으면 새로 저장

     */
}
