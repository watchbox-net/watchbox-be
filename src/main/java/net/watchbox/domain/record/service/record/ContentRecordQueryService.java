package net.watchbox.domain.record.service.record;

import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import net.watchbox.domain.content.dto.interaction.MemberRecord;
import net.watchbox.domain.content.dto.list.ContentItem;
import net.watchbox.domain.content.entity.Content;
import net.watchbox.domain.content.entity.MediaType;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.record.dto.record.request.ContentRecordCountRequest;
import net.watchbox.domain.record.dto.record.request.ContentRecordQueryRequest;
import net.watchbox.domain.record.entity.record.ContentRecord;
import net.watchbox.domain.record.repository.record.ContentRecordQueryRepository;
import net.watchbox.domain.record.repository.record.ContentRecordRepository;
import net.watchbox.global.dto.CursorPayload;
import net.watchbox.global.dto.response.exception.CustomException;
import net.watchbox.global.dto.response.exception.ErrorCode;
import net.watchbox.global.util.CursorCodec;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Observed
public class ContentRecordQueryService {
    private final ContentRecordRepository contentRecordRepository;
    private final ContentRecordQueryRepository contentRecordQueryRepository;
    private final CursorCodec cursorCodec;

    public List<ContentRecord> getMyContentRecordList(Member member, ContentRecordQueryRequest request, int size) {
        CursorPayload cursor = cursorCodec.decode(request.getCursor()); // null 이면 첫 페이지
        return contentRecordQueryRepository.findMyContentRecordList(member, request, cursor, size);
    }

    public Long countByMember(Member member) {
        return contentRecordRepository.countByMember(member);
    }

    public Long countMyContentRecord(Member member, ContentRecordCountRequest request) {
        return contentRecordQueryRepository.countMyContentRecord(
                member, request);
    }

    public ContentRecord getByContentRecordId(Long contentRecordId) {
        return contentRecordRepository.findById(contentRecordId)
                .orElseThrow(() -> new CustomException(ErrorCode.CONTENT_RECORD_NOT_FOUND));
    }

    public List<ContentRecord> getByMemberAndTmdbIdsAndMediaType(Member member, List<Long> tmdbIds, MediaType mediaType) {
        return contentRecordRepository.findContentRecordsByMemberAndTmdbIdsAndMediaType(member, tmdbIds, mediaType);
    }

    public List<ContentRecord> getByMemberAndContentIds(Member member, List<Long> contentIds) {
        return contentRecordRepository.findByMemberAndContentIdIn(member, contentIds);
    }

    public ContentRecord getByMemberAndContent(Member member, Content content){
        return contentRecordRepository.findByMemberAndContent(member, content)
                .orElse(null); // 의도적 Null 반환 - 없을 경우 프론트에서 배당 안함
    }

    public long countLikedContentsByMember(Member member) {
        return contentRecordRepository.countByMemberAndLikedTrue(member);
    }

    public long countWatchStatusByMember(Member member) {
        return contentRecordRepository.countByMemberAndWatchStatusIsNotNull(member);
    }

    public List<ContentRecord> getWatchRecordsWithContent(Member member) {
        return contentRecordRepository.findWatchRecordsWithContent(member);
    }

//    public ContentRecordResponse getRecordInfo(Long recordId) {
//        ContentRecord contentRecord = contentRecordRepository.findById(recordId)
//                .orElseThrow(() -> new CustomException(ErrorCode.CONTENT_RECORD_NOT_FOUND));
//        return ContentRecordResponse.from(contentRecord);
//    }

    // 로그인 사용자의 ContentRecord를 각 콘텐츠에 후처리로 병합
    public List<ContentItem> attachMemberRecord(List<ContentItem> items, Member member, MediaType mediaType) {
        List<Long> tmdbIds = items.stream()
                .map(item -> item.getContentSummary().getTmdbId())
                .toList();

        Map<Long, ContentRecord> recordMap = getByMemberAndTmdbIdsAndMediaType(member, tmdbIds, mediaType)
                .stream()
                .collect(Collectors.toMap(
                        r -> r.getContent().getTmdbId(),
                        r -> r
                ));

        return applyRecordMap(items, recordMap);
    }

    /**
     * 여러 섹션을 <b>mediaType 당 한 번의 IN 쿼리</b>로 개인화한다.
     *
     * <p>섹션마다 {@link #attachMemberRecord} 를 부르면 섹션 수만큼 쿼리가 나간다.
     * 홈(movie 4 + tv 4 섹션)은 8회 → 이 메서드로 mediaType 당 1회, 총 2회가 된다.
     *
     * @return 입력 {@code sections} 와 같은 순서·크기의 리스트
     */
    public List<List<ContentItem>> attachMemberRecordBatch(
            List<List<ContentItem>> sections, Member member, MediaType mediaType) {
        List<Long> tmdbIds = sections.stream()
                .flatMap(List::stream)
                .map(item -> item.getContentSummary().getTmdbId())
                .distinct()
                .toList();

        if (tmdbIds.isEmpty()) {
            return sections;
        }

        Map<Long, ContentRecord> recordMap = getByMemberAndTmdbIdsAndMediaType(member, tmdbIds, mediaType)
                .stream()
                .collect(Collectors.toMap(
                        r -> r.getContent().getTmdbId(),
                        r -> r
                ));

        return sections.stream()
                .map(items -> applyRecordMap(items, recordMap))
                .toList();
    }

    // 조회해둔 record 맵을 각 아이템에 병합 (쿼리 없음)
    private static List<ContentItem> applyRecordMap(List<ContentItem> items, Map<Long, ContentRecord> recordMap) {
        return items.stream()
                .map(item -> ContentItem.builder()
                        .contentSummary(item.getContentSummary())
                        .memberRecord(MemberRecord.from(
                                recordMap.get(item.getContentSummary().getTmdbId())))
                        .build())
                .toList();
    }

    public void validateMember(ContentRecord contentRecord, Member member) {
        if (!contentRecord.getMember().getMemberId().equals(member.getMemberId())) {
            throw new CustomException(ErrorCode.NOT_RECORD_MEMBER);
        }
    }
}
