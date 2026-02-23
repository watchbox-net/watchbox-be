package net.watchbox.domain.record.facade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.content.base.entity.Content;
import net.watchbox.domain.content.base.service.ContentCommandService;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.record.dto.request.ContentLikeUpsertRequest;
import net.watchbox.domain.record.dto.response.ContentRecordPageResponse;
import net.watchbox.domain.record.dto.request.WatchStatusUpsertRequest;
import net.watchbox.domain.record.dto.response.ContentRecordResponse;
import net.watchbox.domain.record.entity.ContentRecord;
import net.watchbox.domain.record.service.ContentRecordService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContentRecordFacade {
    private final ContentRecordService contentRecordService;
    private final ContentCommandService contentCommandService;

    @Transactional(readOnly = true)
    public ContentRecordPageResponse getWatchStatusList(Member member) {
        // 시청 기록 중에 시청 상태가 null이 아닌 것들만 조회
        List<ContentRecord> contentRecords = contentRecordService.getContentRecordsByMemberAndNotNullWatchStatus(member);
        return ContentRecordPageResponse.builder()
                .watchRecordList(contentRecords.stream().map(ContentRecordResponse::from).toList())
                .totalCount(contentRecords.size())
                .build();
    }

    @Transactional
    public ContentRecordResponse upsertWatchStatus(Member member, WatchStatusUpsertRequest request) {
        // Content 조회 or 저장
        Content content = contentCommandService.getOrSaveContentCascade(request.getContentId(),
                request.getWatchMediaType().toMediaType());

        // ContentRecord 조회 or 생성
        ContentRecord contentRecord = contentRecordService.getOrCreate(member, content);

        // WatchStatus 업데이트
        contentRecord.updateWatchStatus(request.getWatchStatus());

        return ContentRecordResponse.from(contentRecord);
    }

    @Transactional
    public void deleteWatchStatus(Member member, Long recordId) {
        ContentRecord contentRecord = contentRecordService.getByContentRecordId(recordId);

        // 시청 기록한 사용자인지 검증
        contentRecordService.validateMember(contentRecord, member);

        // WatchStatus 삭제
        contentRecordService.deleteWatchStatus(contentRecord);
    }

    @Transactional(readOnly = true)
    public ContentRecordPageResponse getContentLikeList(Member member) {
        // 시청 기록 중에 좋아요 상태가 true인 것들만 조회
        List<ContentRecord> contentRecords = contentRecordService.getContentRecordsByMemberAndLikedTrue(member);
        return ContentRecordPageResponse.builder()
                .watchRecordList(contentRecords.stream().map(ContentRecordResponse::from).toList())
                .totalCount(contentRecords.size())
                .build();
    }

    @Transactional
    public ContentRecordResponse upsertContentLike(Member member, ContentLikeUpsertRequest request) {
        // Content 조회 or 저장
        Content content = contentCommandService.getOrSaveContentCascade(request.getContentId(), request.getMediaType());

        // ContentRecord 조회 or 생성
        ContentRecord contentRecord = contentRecordService.getOrCreate(member, content);

        // Liked 업데이트
        contentRecord.updateLiked(request.getLiked());

        return ContentRecordResponse.from(contentRecord);
    }

    @Transactional
    public void deleteContentLike(Member member, Long recordId) {
        ContentRecord contentRecord = contentRecordService.getByContentRecordId(recordId);

        // 좋아요한 사용자인지 검증
        contentRecordService.validateMember(contentRecord, member);

        // Liked 삭제
        contentRecordService.deleteLiked(contentRecord);

    }

}
