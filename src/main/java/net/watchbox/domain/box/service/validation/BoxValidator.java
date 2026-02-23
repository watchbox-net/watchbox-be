package net.watchbox.domain.box.service.validation;

import lombok.RequiredArgsConstructor;
import net.watchbox.domain.box.entity.box.Box;
import net.watchbox.domain.box.entity.content.BoxContent;
import net.watchbox.domain.box.entity.invitation.BoxInvitation;
import net.watchbox.domain.box.entity.invitation.RequestStatus;
import net.watchbox.domain.box.entity.member.BoxMember;
import net.watchbox.domain.box.entity.member.BoxMemberRole;
import net.watchbox.domain.box.repository.invitation.BoxInvitationRepository;
import net.watchbox.domain.box.repository.member.BoxMemberRepository;
import net.watchbox.domain.box.repository.content.BoxContentRepository;
import net.watchbox.domain.content.common.entity.Content;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.global.dto.response.exception.CustomException;
import org.springframework.stereotype.Service;

import static net.watchbox.global.dto.response.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class BoxValidator {
    private final BoxContentRepository boxContentRepository;
    private final BoxMemberRepository boxMemberRepository;
    private final BoxInvitationRepository boxInvitationRepository;

    // ToDo: 접근 상황별로 예외 멘트 다르게 나오도록 나눠야함
    // ------------------- Box CRUD 권한 검증 -------------------

    // 해당 BoxMember인지 검증
    public void validateBoxMember(Box box, Member member) {
        if(!boxMemberRepository.existsByBoxAndMember(box, member)) {
            throw new CustomException(NOT_BOX_MEMBER, member.getMemberId(), "Member");
        }
    }

    public void validateBoxOwner(Box box, Member member) {
        // BoxMember 조회
        BoxMember boxMember = boxMemberRepository.findByBoxAndMember(box, member)
                .orElseThrow(() -> new CustomException(BOX_MEMBER_NOT_FOUND, member.getMemberId(), "Member"));
        // Role이 OWNER 인지
        if (boxMember.getRole() != BoxMemberRole.OWNER) {
            throw new CustomException(FORBIDDEN_BOX_ACCESS, member.getMemberId(), "Member");
        }
    }

    public void validateBoxEditor(Box box, Member member) {
        // BoxMember 조회
        BoxMember boxMember = boxMemberRepository.findByBoxAndMember(box, member)
                .orElseThrow(() -> new CustomException(BOX_MEMBER_NOT_FOUND, member.getMemberId(), "Member"));
        // Role이 OWNER 또는 EDITOR 인지
        if (boxMember.getRole() != BoxMemberRole.OWNER && boxMember.getRole() != BoxMemberRole.EDITOR) {
            throw new CustomException(FORBIDDEN_BOX_ACCESS, member.getMemberId(), "Member");
        }
    }

    // ------------------- InviteBoxRequest 검증 -------------------

    // 공유 박스의 기존 멤버로 이미 존재하는지 검증
    public void validateExistingBoxMember(Box box, Member member) {
        if(boxMemberRepository.existsByBoxAndMember(box, member)) {
            throw new CustomException(ALREADY_BOX_MEMBER, member.getMemberId(), "Member");
        }
    }

    // ------------------- BoxContent 존재 검증 -------------------
    // 박스에 이미 추가된 컨텐츠인지 확인
    public boolean contentExistsInBox(Box box, Content content) {
        return boxContentRepository.existsByBoxAndContent(box, content);
    }

    // 박스에 이미 추가된 컨텐츠인지 검증
    public void validateContentNotInBox(Box box, Content content) {
        if(contentExistsInBox(box, content)) {
            throw new CustomException(BOX_CONTENT_ALREADY_IN_BOX);
        }
    }

    // 해당 멤버로 이미 추가된 컨텐츠인지 확인
    public boolean contentExistsInSharedBox(Member member, Box box, Content content) {
        return boxContentRepository.existsByPublisherAndBoxAndContent(member, box, content);
    }

    // 해당 멤버로 이미 추가된 컨텐츠인지 검증
    public void validateContentNotInSharedBox(Member member, Box box, Content content) {
        if(contentExistsInSharedBox(member, box, content)) {
            throw new CustomException(CONTENT_ALREADY_IN_SHARED_BOX, member.getMemberId());
        }
    }

    // ------------------- BoxContent 추가/삭제 권한 검증 -------------------

    public void validateBoxContentAdder(Box box, Member member) {
        // BoxMember 조회
        BoxMember boxMember = boxMemberRepository.findByBoxAndMember(box, member)
                .orElseThrow(() -> new CustomException(BOX_MEMBER_NOT_FOUND, member.getMemberId(), "Member"));
        // Role이 충분한지
        if (boxMember.getRole() == BoxMemberRole.VIEWER) {
            throw new CustomException(INSUFFICIENT_BOX_CONTENT_EDIT_PERMISSION, member.getMemberId(), "Member");
        }
    }

    public void validateBoxContentRemover(Member member, BoxContent boxContent) {
        // BoxContent 추가한 멤버인지
        if(!boxContent.getPublisher().getMemberId().equals(member.getMemberId())) {
            throw new CustomException(FORBIDDEN_CONTENT_REMOVAL, member.getMemberId(), "Member");
        }
    }

    // ------------------- BoxInvitation 검증 -------------------
    // 수신자 본인인지 검증
    public void validateReceiver(Member actor, Member receiver) {
        if (!actor.getMemberId().equals(receiver.getMemberId())) {
            throw new CustomException(REQUEST_UNAUTHORIZED_ACCESS);
        }
    }

    // 중복 초대 요청 검증
    public void validateDuplicateInviteRequest(Box box, Member receiver) {
        if (boxInvitationRepository.existsByBoxAndReceiverAndStatus(box, receiver, RequestStatus.PENDING)) {
            throw new CustomException(DUPLICATE_INVITE_REQUEST, receiver.getMemberId(), "Member");
        }
    }

    // 이미 처리된 요청인지지 검증 (대기중인 요청 상태인지 검증)
    public void validatePendingInviteRequest(BoxInvitation boxInvitation) {
        if (boxInvitation.getStatus() != RequestStatus.PENDING) {
            throw new CustomException(INVITATION_ALREADY_RESPONDED, boxInvitation.getRequestId());
        }
    }

}
