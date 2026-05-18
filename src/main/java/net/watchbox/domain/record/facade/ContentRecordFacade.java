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
import net.watchbox.domain.record.dto.request.ContentLikeUpsertRequest;
import net.watchbox.domain.record.dto.request.ContentRecordQueryRequest;
import net.watchbox.domain.record.dto.request.WatchStatusUpsertRequest;
import net.watchbox.domain.record.dto.response.ContentRecordCountResponse;
import net.watchbox.domain.record.dto.response.ContentRecordResponse;
import net.watchbox.domain.record.entity.ContentRecord;
import net.watchbox.domain.record.repository.ContentRecordCursorBuilder;
import net.watchbox.domain.record.service.ContentRecordCommandService;
import net.watchbox.domain.record.service.ContentRecordQueryService;
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
    private final ContentCommandService contentCommandService;
    private final CursorCodec cursorCodec;

    @Transactional(readOnly = true)
    public ContentCursorPageResponse getMyRecordedContentPage(Member member, ContentRecordQueryRequest request) {
        // AppConstants.PAGE_SIZE+1 개를 받음 - hasNext 판단용
        List<ContentRecord> fetched = contentRecordQueryService.getMyContentRecordList(member, request, AppConstants.PAGE_SIZE);

        boolean hasNext = fetched.size() > AppConstants.PAGE_SIZE;
        List<ContentRecord> page = hasNext ? fetched.subList(0, AppConstants.PAGE_SIZE) : fetched;

        String nextCursor = hasNext
                ? cursorCodec.encode(ContentRecordCursorBuilder.build(page.get(page.size() - 1), request))
                : null;

        List<ContentItem> contentItemList = ContentRecordMapper.toContentItems(page);

        return ContentCursorPageResponse.builder()
                .contentItemList(contentItemList)
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .build();
    }

    @Transactional(readOnly = true)
    public ContentRecordCountResponse getMyContentRecordCount(Member member) {
        return ContentRecordCountResponse.of(contentRecordQueryService.countByMember(member));
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

        // WatchStatus 업데이트
        contentRecord.updateWatchStatus(request.getWatchStatus());

        return ContentRecordResponse.from(contentRecord);
    }

    @Transactional
    public void deleteWatchStatus(Member member, Long recordId) {
        ContentRecord contentRecord = contentRecordQueryService.getByContentRecordId(recordId);

        // 시청 기록한 사용자인지 검증
        contentRecordQueryService.validateMember(contentRecord, member);

        // WatchStatus 삭제
        contentRecordCommandService.deleteWatchStatus(contentRecord);
    }

    @Transactional
    public ContentRecordResponse upsertContentLike(Member member, ContentLikeUpsertRequest request) {
        // Content 조회 or 저장
        Content content = contentCommandService.getOrSaveContentCascade(request.getTmdbId(), request.getMediaType());

        // ContentRecord 조회 or 생성
        ContentRecord contentRecord = contentRecordCommandService.getOrCreate(member, content);

        // Liked 업데이트
        contentRecord.updateLiked(request.getLiked());

        return ContentRecordResponse.from(contentRecord);
    }

    @Transactional
    public void deleteContentLike(Member member, Long recordId) {
        ContentRecord contentRecord = contentRecordQueryService.getByContentRecordId(recordId);

        // 좋아요한 사용자인지 검증
        contentRecordQueryService.validateMember(contentRecord, member);

        // Liked 삭제
        contentRecordCommandService.deleteLiked(contentRecord);

    }


}
