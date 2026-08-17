package net.watchbox.domain.record.service.record;

import lombok.RequiredArgsConstructor;
import net.watchbox.domain.content.entity.Content;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.record.entity.record.ContentRecord;
import net.watchbox.domain.record.entity.record.WatchStatus;
import net.watchbox.domain.record.repository.record.ContentRecordRepository;
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
        if(!Boolean.TRUE.equals(contentRecord.getLiked())){
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
