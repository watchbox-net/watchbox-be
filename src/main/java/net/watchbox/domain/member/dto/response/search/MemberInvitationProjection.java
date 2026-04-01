package net.watchbox.domain.member.dto.response.search;

import net.watchbox.domain.box.entity.invitation.RequestStatus;

public interface MemberInvitationProjection {
    Long getMemberId();
    String getNickname();
    String getProfileImage();
    RequestStatus getStatus();
}
