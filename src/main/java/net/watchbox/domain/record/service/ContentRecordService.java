package net.watchbox.domain.record.service;

import lombok.RequiredArgsConstructor;
import net.watchbox.domain.content.base.entity.Content;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.record.dto.response.ContentRecordResponse;
import net.watchbox.domain.record.entity.ContentRecord;
import net.watchbox.domain.record.repository.ContentRecordRepository;
import net.watchbox.global.dto.response.exception.CustomException;
import net.watchbox.global.dto.response.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ContentRecordService {
    private final ContentRecordRepository contentRecordRepository;

    @Transactional
    public ContentRecord getOrCreate(Member member, Content content) {
        return contentRecordRepository.findByMemberAndContent(member, content)
                .orElseGet(() -> contentRecordRepository.save(
                        ContentRecord.builder()
                                .member(member)
                                .content(content)
                                .build()
                ));
    }

    public ContentRecord getByContentRecordId(Long contentRecordId) {
        return contentRecordRepository.findById(contentRecordId)
                .orElseThrow(() -> new CustomException(ErrorCode.WATCH_RECORD_NOT_FOUND));
    }

    public List<ContentRecord> getByMemberAndContentIdIn(Member member, List<Long> contentIds) {
        return contentRecordRepository.findContentRecordsByMemberAndContentIdIn(member, contentIds);
    }

    public long countLikedContentsByMember(Member member) {
        return contentRecordRepository.countByMemberAndLikedTrue(member);
    }

    public long countWatchStatusByMember(Member member) {
        return contentRecordRepository.countByMemberAndWatchStatusIsNotNull(member);
    }

    public void validateMember(ContentRecord contentRecord, Member member) {
        if (!contentRecord.getMember().getMemberId().equals(member.getMemberId())) {
            throw new CustomException(ErrorCode.NOT_RECORD_MEMBER);
        }
    }

    @Transactional
    public void deleteWatchStatus(ContentRecord contentRecord) {
        contentRecord.updateWatchStatus(null);
        if(contentRecord.getLiked() == null){
            contentRecordRepository.delete(contentRecord);
        }
    }

    public List<ContentRecord> getWatchRecordsWithContent(Member member) {
        return contentRecordRepository.findWatchRecordsWithContent(member);
    }

    @Transactional
    public void deleteLiked(ContentRecord contentRecord) {
        contentRecord.updateLiked(null);
        if(contentRecord.getWatchStatus() == null){
            contentRecordRepository.delete(contentRecord);
        }
    }

    public List<ContentRecord> getLikedRecordsWithContent(Member member) {
        return contentRecordRepository.findLikedRecordsWithContent(member, true);
    }

    public ContentRecordResponse getRecordInfo(Long recordId) {
        ContentRecord contentRecord = contentRecordRepository.findById(recordId)
                .orElseThrow(() -> new CustomException(ErrorCode.WATCH_RECORD_NOT_FOUND));
        return ContentRecordResponse.from(contentRecord);
    }
}
