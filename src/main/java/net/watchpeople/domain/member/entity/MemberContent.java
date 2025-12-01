package net.watchpeople.domain.member.entity;

import jakarta.persistence.*;
import lombok.*;
import net.watchpeople.domain.content.TmdbContent;
import net.watchpeople.global.entity.BaseTime;

import java.time.LocalDate;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity
public class MemberContent extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long statusId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tmdb_id", nullable = false)
    private TmdbContent tmdbContent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WatchStatus watchStatus;

    private Boolean isLiked; // 좋아요 여부

    private Double rating; // 평점

    private LocalDate watchedDate; // 시청 완료일
}
