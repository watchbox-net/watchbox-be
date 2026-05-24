package net.watchbox.domain.record.service;

import lombok.RequiredArgsConstructor;
import net.watchbox.domain.content.entity.Content;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.record.entity.ContentRecord;
import net.watchbox.domain.record.entity.WatchStatus;
import net.watchbox.domain.record.repository.ContentRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional
public class ContentRecordCommandService {
    private final ContentRecordRepository contentRecordRepository;

    public ContentRecord getOrCreate(Member member, Content content) {
        return contentRecordRepository.findByMemberAndContent(member, content)
                .orElseGet(() -> contentRecordRepository.save(
                        ContentRecord.builder()
                                .member(member)
                                .content(content)
                                .mediaType(content.getMediaType())
                                .build()
                ));
    }

    public void upsertWatchStatus(ContentRecord contentRecord, WatchStatus watchStatus) {
        contentRecord.updateWatchStatus(watchStatus);
        contentRecordRepository.save(contentRecord);
    }

    public void deleteWatchStatus(ContentRecord contentRecord) {
        contentRecord.updateWatchStatus(null);
        if(contentRecord.getLiked() == null){
            contentRecordRepository.delete(contentRecord);
        }
    }

    public void deleteLiked(ContentRecord contentRecord) {
        contentRecord.updateLiked(null);
        if(contentRecord.getWatchStatus() == null){
            contentRecordRepository.delete(contentRecord);
        }
    }

}
