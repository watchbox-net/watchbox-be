package net.watchpeople.domain.box.entity.content;

import jakarta.persistence.*;
import lombok.*;
import net.watchpeople.domain.content.common.entity.Content;
import net.watchpeople.domain.content.common.entity.MediaType;
import net.watchpeople.domain.member.entity.Member;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity
public class MyContent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long myContentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tmdb_id", nullable = false)
    private Content content;

    private MediaType mediaType;
}
