package net.watchbox.domain.member.dto.response.search;

import net.watchbox.domain.box.entity.invitation.RequestStatus;

public enum BoxInviteStatus {
    NONE,           // 초대 요청 보낸 적 없음
    PENDING,        // 초대 요청 보낸 후 대기중
    MEMBER;          // 박스 멤버

    public static BoxInviteStatus from(RequestStatus status) {
        if (status == null) return BoxInviteStatus.NONE;
        return switch (status) {
            case RequestStatus.PENDING  -> BoxInviteStatus.PENDING;
            case RequestStatus.ACCEPTED -> BoxInviteStatus.MEMBER;
            case RequestStatus.REJECTED -> BoxInviteStatus.NONE;
        };
    }
}
