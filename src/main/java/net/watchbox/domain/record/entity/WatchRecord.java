package net.watchbox.domain.record.entity;

import jakarta.persistence.*;
import lombok.*;
import net.watchbox.domain.content.common.entity.Content;
import net.watchbox.domain.content.common.entity.MediaType;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.global.entity.BaseTime;

import java.time.LocalDate;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity
public class WatchRecord extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long watchRecordId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tmdb_id", nullable = false)
    private Content content;

    @Enumerated(EnumType.STRING)
    private WatchStatus watchStatus;

    private Boolean liked; // 좋아요 여부

    private LocalDate watchedDate; // 시청 완료일

    @Column(name = "media_type")
    @Enumerated(EnumType.STRING)
    private MediaType mediaType;

//    private Double rating; // 평점

}
