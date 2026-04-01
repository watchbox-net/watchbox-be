package net.watchbox.domain.content.base.dto.interaction;

import lombok.Getter;
import lombok.ToString;
import net.watchbox.domain.member.entity.Member;

@Getter
@ToString
public class PublisherSummary {
    private Long publisherId;
    private String nickname;
    private String profileImage;

    public static PublisherSummary from(Member member) {
        PublisherSummary publisherSummary = new PublisherSummary();
        publisherSummary.publisherId = member.getMemberId();
        publisherSummary.nickname = member.getNickname();
        publisherSummary.profileImage = member.getProfileImage();
        return publisherSummary;
    }
}
