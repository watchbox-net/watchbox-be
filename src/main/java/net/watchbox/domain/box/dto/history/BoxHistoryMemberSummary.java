package net.watchbox.domain.box.dto.history;

import net.watchbox.domain.member.entity.Member;

/**
 * 히스토리 아이템의 actor / targetMember 표현.
 * 탈퇴한 사용자(Member null)면 null 반환.
 */
public record BoxHistoryMemberSummary(Long memberId, String nickname) {
    public static BoxHistoryMemberSummary from(Member member) {
        return member == null ? null : new BoxHistoryMemberSummary(member.getMemberId(), member.getNickname());
    }
}
