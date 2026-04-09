package net.watchbox.domain.record.entity;

import jakarta.persistence.*;
import lombok.*;
import net.watchbox.domain.content.entity.Content;
import net.watchbox.domain.content.entity.MediaType;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.global.entity.BaseTime;

import java.time.LocalDate;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity
public class ContentRecord extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long contentRecordId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = false)
    private Content content;

    @Enumerated(EnumType.STRING)
    private WatchStatus watchStatus;

    private Boolean liked; // true: 좋아요, false: 미등록 응답용 | 싫어요는 따로 저장X

    private LocalDate watchedDate; // 시청 완료일

    @Column(name = "media_type")
    @Enumerated(EnumType.STRING)
    private MediaType mediaType;

//    private Double rating; // 평점

    public void updateWatchStatus(WatchStatus watchStatus){
        this.watchStatus = watchStatus;
        if (watchStatus == WatchStatus.COMPLETED) {
            this.watchedDate = LocalDate.now();
        } else {
            this.watchedDate = null;
        }
    }

    public void updateLiked(Boolean liked){
        this.liked = liked;
    }
}
