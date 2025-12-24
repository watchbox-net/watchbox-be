package net.watchbox.domain.box.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.box.entity.SharedBox;
import net.watchbox.domain.box.entity.content.SharedBoxContent;
import net.watchbox.domain.box.entity.member.BoxMember;
import net.watchbox.domain.box.entity.member.BoxMemberRole;
import net.watchbox.domain.box.repository.BoxMemberRepository;
import net.watchbox.domain.box.repository.SharedBoxRepository;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.global.dto.response.exception.CustomException;
import net.watchbox.global.dto.response.exception.ErrorCode;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BoxMemberService {
    private final BoxMemberRepository boxMemberRepository;
    private final SharedBoxRepository sharedBoxRepository;

    public void validateSharedBoxAdder(SharedBox sharedBox, Member member) {
        // BoxMember 인지
        BoxMember boxMember = boxMemberRepository.findBySharedBoxAndMember(sharedBox, member)
                .orElseThrow(() -> new CustomException(ErrorCode.BOX_MEMBER_NOT_FOUND, member.getMemberId(), "member"));
        // Role이 충분한지
        if (boxMember.getRole() == BoxMemberRole.VIEWER) {
            throw new CustomException(ErrorCode.FORBIDDEN_BOX_ACCESS, member.getMemberId(), "member");
        }
    }

    public void validateSharedBoxRemover(SharedBox sharedBox, Member member, SharedBoxContent sharedBoxContent) {
        // 추가한 회원인지
        if(!sharedBoxContent.getAddedBy().equals(member)) {
            throw new CustomException(ErrorCode.FORBIDDEN_BOX_ACCESS, member.getMemberId());
        }
        // BoxMember 인지
        BoxMember boxMember = boxMemberRepository.findBySharedBoxAndMember(sharedBox, member)
                .orElseThrow(() -> new CustomException(ErrorCode.BOX_MEMBER_NOT_FOUND, member.getMemberId(), "member"));
        // Role이 충분한지
        if (boxMember.getRole() == BoxMemberRole.VIEWER) {
            throw new CustomException(ErrorCode.FORBIDDEN_BOX_ACCESS, member.getMemberId(), "member");
        }
    }
}
