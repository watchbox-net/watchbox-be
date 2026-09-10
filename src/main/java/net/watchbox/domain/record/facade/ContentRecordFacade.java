package net.watchbox.domain.record.facade;

import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.content.dto.list.ContentCursorPageResponse;
import net.watchbox.domain.content.dto.list.ContentItem;
import net.watchbox.domain.content.entity.Content;
import net.watchbox.domain.content.mapper.record.ContentRecordMapper;
import net.watchbox.domain.content.service.ContentCommandService;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.record.dto.record.request.ContentLikeUpsertRequest;
import net.watchbox.domain.record.dto.record.request.ContentRecordCountRequest;
import net.watchbox.domain.record.dto.record.request.ContentRecordQueryRequest;
import net.watchbox.domain.record.dto.record.request.WatchStatusUpsertRequest;
import net.watchbox.domain.record.dto.record.response.ContentRecordCountResponse;
import net.watchbox.domain.record.dto.record.response.ContentRecordResponse;
import net.watchbox.domain.record.entity.record.ContentRecord;
import net.watchbox.domain.record.entity.record.WatchStatus;
import net.watchbox.domain.record.repository.record.ContentRecordCursorBuilder;
import net.watchbox.domain.record.service.history.ContentRecordHistoryCommandService;
import net.watchbox.domain.record.service.record.ContentRecordCommandService;
import net.watchbox.domain.record.service.record.ContentRecordQueryService;
import net.watchbox.global.constants.AppConstants;
import net.watchbox.global.util.CursorCodec;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@Observed
public class ContentRecordFacade {
    private final ContentRecordQueryService contentRecordQueryService;
    private final ContentRecordCommandService contentRecordCommandService;
    private final ContentRecordHistoryCommandService contentRecordHistoryCommandService;
    private final ContentCommandService contentCommandService;
    private final CursorCodec cursorCodec;

    @Transactional(readOnly = true)
    public ContentCursorPageResponse getMyRecordedContentPage(Member member, ContentRecordQueryRequest request) {
        // AppConstants.PAGE_SIZE+1 개를 받음 - hasNext 판단용
        List<ContentRecord> fetched = contentRecordQueryService.getMyContentRecordList(member, request, AppConstants.PAGE_SIZE);

        boolean hasNext = fetched.size() > AppConstants.PAGE_SIZE;
        List<ContentRecord> page = hasNext ? fetched.subList(0, AppConstants.PAGE_SIZE) : fetched;

        String nextCursor = hasNext
                ? cursorCodec.encode(ContentRecordCursorBuilder.build(page.getLast(), request))
                : null;

        List<ContentItem> contentItemList = ContentRecordMapper.toContentItems(page);

        return ContentCursorPageResponse.builder()
                .contentItemList(contentItemList)
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .build();
    }

    @Transactional(readOnly = true)
    public ContentRecordCountResponse getMyContentRecordCount(Member member, ContentRecordCountRequest request) {
        return ContentRecordCountResponse.of(contentRecordQueryService.countMyContentRecord(member, request));
    }

//    @Transactional(readOnly = true)
//    public ContentPageResponse getWatchStatusList(Member member) {
//        log.debug("Login Member: {}", member.getMemberId());
//        // 시청 기록이 등록된 ContentRecord 리스트 조회
//        List<ContentRecord> contentRecords = contentRecordQueryService.getWatchRecordsWithContent(member);
//
//        List<ContentItem> contentItemList =  ContentRecordMapper.toContentItems(contentRecords);
//        return ContentPageResponse.builder()
//                .contentItemList(contentItemList)
//                .totalCount((long) contentItemList.size())
//                .build();
//    }

    @Transactional
    public ContentRecordResponse upsertWatchStatus(Member member, WatchStatusUpsertRequest request) {
        // Content 조회 or 저장
        Content content = contentCommandService.getOrSaveContentCascade(request.getTmdbId(),
                request.getWatchMediaType().toMediaType());

        // ContentRecord 조회 or 생성
        ContentRecord contentRecord = contentRecordCommandService.getOrCreate(member, content);

        // 변경 전 상태 스냅샷 (히스토리용)
        WatchStatus oldStatus = contentRecord.getWatchStatus();
        WatchStatus newStatus = request.getWatchStatus();

        // WatchStatus 업데이트
        contentRecordCommandService.upsertWatchStatus(contentRecord, newStatus);

        // 히스토리 기록 — 첫 등록 / 변경 분기
        if (oldStatus == null) {
            // 시청 상태가 없던 콘텐츠 → 최초 등록
            contentRecordHistoryCommandService.watchStatusRegister(member, content, newStatus);
        } else if (oldStatus != newStatus) {
            // 기존 상태 있음 + 실제로 바뀐 경우만 → 변경
            contentRecordHistoryCommandService.watchStatusChange(member, content, oldStatus, newStatus);
        }

        return ContentRecordResponse.from(contentRecord);
    }

    @Transactional
    public void deleteWatchStatus(Member member, Long recordId) {
        ContentRecord contentRecord = contentRecordQueryService.getByContentRecordId(recordId);

        // 시청 기록한 사용자인지 검증
        contentRecordQueryService.validateMember(contentRecord, member);

        // 변경 전 상태 스냅샷 (히스토리용)
        WatchStatus oldStatus = contentRecord.getWatchStatus();

        // WatchStatus 삭제
        contentRecordCommandService.deleteWatchStatus(contentRecord);

        // 히스토리 기록 — 상태가 있었으면 (null → null 은 기록 안 함)
        if (oldStatus != null) {
            contentRecordHistoryCommandService.watchStatusChange(
                    member, contentRecord.getContent(), oldStatus, null);
        }
    }

    @Transactional
    public ContentRecordResponse upsertContentLike(Member member, ContentLikeUpsertRequest request) {
        // Content 조회 or 저장
        Content content = contentCommandService.getOrSaveContentCascade(request.getTmdbId(), request.getMediaType());

        // ContentRecord 조회 or 생성
        ContentRecord contentRecord = contentRecordCommandService.getOrCreate(member, content);

        // 변경 전 좋아요 스냅샷 (히스토리용)
        Boolean before = contentRecord.getLiked();
        Boolean after = request.getLiked();

        // Liked 업데이트
        contentRecord.updateLiked(after);

        // 히스토리 기록 — 실제로 바뀐 경우에만
        if (!java.util.Objects.equals(before, after)) {
            if (Boolean.TRUE.equals(after)) {
                contentRecordHistoryCommandService.likeAdded(member, content);
            } else {
                contentRecordHistoryCommandService.likeRemoved(member, content);
            }
        }

        return ContentRecordResponse.from(contentRecord);
    }

    @Transactional
    public void deleteContentLike(Member member, Long recordId) {
        ContentRecord contentRecord = contentRecordQueryService.getByContentRecordId(recordId);

        // 좋아요한 사용자인지 검증
        contentRecordQueryService.validateMember(contentRecord, member);

        // 변경 전 좋아요 스냅샷 (히스토리용)
        Boolean before = contentRecord.getLiked();

        // Liked 삭제
        contentRecordCommandService.deleteLiked(contentRecord);

        // 히스토리 기록 — 좋아요가 등록돼 있었을 때만
        if (Boolean.TRUE.equals(before)) {
            contentRecordHistoryCommandService.likeRemoved(member, contentRecord.getContent());
        }
    }


}
