package net.watchbox.domain.record.service;

import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import net.watchbox.domain.content.dto.interaction.MemberRecord;
import net.watchbox.domain.content.dto.list.ContentItem;
import net.watchbox.domain.content.entity.MediaType;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.record.dto.response.ContentRecordResponse;
import net.watchbox.domain.record.entity.ContentRecord;
import net.watchbox.domain.record.repository.ContentRecordRepository;
import net.watchbox.global.dto.response.exception.CustomException;
import net.watchbox.global.dto.response.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Observed
public class ContentRecordQueryService {
    private final ContentRecordRepository contentRecordRepository;

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

    public ContentRecord getByMemberIdAndTmdbIdOrElseNull(Long memberId, Long tmdbId, MediaType mediaType) {
        return contentRecordRepository.findByMember_MemberIdAndContent_TmdbIdAndContent_MediaType(memberId, tmdbId, mediaType)
                .orElse(null);
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

    public List<ContentRecord> getLikedRecordsWithContent(Member member) {
        return contentRecordRepository.findLikedRecordsWithContent(member, true);
    }

    public ContentRecordResponse getRecordInfo(Long recordId) {
        ContentRecord contentRecord = contentRecordRepository.findById(recordId)
                .orElseThrow(() -> new CustomException(ErrorCode.CONTENT_RECORD_NOT_FOUND));
        return ContentRecordResponse.from(contentRecord);
    }

    // 로그인 사용자의 ContentRecord를 각 컨텐츠에 후처리로 병합
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
