package net.watchbox.domain.box.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.box.entity.SharedBox;
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

    public void validateSharedBoxAdder(Long boxId, Member member) {
        // SharedBox 존재하는지, BoxMember 인지, Role이 충분한지
        SharedBox sharedBox = sharedBoxRepository.findById(boxId)
                .orElseThrow(() -> new CustomException(ErrorCode.SHARED_BOX_NOT_FOUND, boxId));
        BoxMember boxMember = boxMemberRepository.findBySharedBoxAndMember(sharedBox, member)
                .orElseThrow(() -> new CustomException(ErrorCode.BOX_MEMBER_NOT_FOUND, member.getMemberId(), "member"));
        if (boxMember.getRole() == BoxMemberRole.VIEWER) {
            throw new CustomException(ErrorCode.FORBIDDEN_BOX_ACCESS, member.getMemberId(), "member");
        }
    }
}
