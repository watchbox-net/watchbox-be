package net.watchbox.domain.box.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.box.entity.SharedBox;
import net.watchbox.domain.box.entity.content.SharedBoxContent;
import net.watchbox.domain.box.entity.member.BoxMember;
import net.watchbox.domain.box.entity.member.BoxMemberRole;
import net.watchbox.domain.box.repository.BoxMemberRepository;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.global.dto.response.exception.CustomException;
import net.watchbox.global.dto.response.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BoxMemberService {
    private final BoxMemberRepository boxMemberRepository;

    public List<BoxMember> findAllBySharedBox(SharedBox sharedBox) {
        return boxMemberRepository.findAllBySharedBox(sharedBox);
    }

    public List<BoxMember> findAllByMember(Member member) {
        return boxMemberRepository.findAllByMember(member);
    }

    @Transactional
    public void addOwnerToBox(Member member, SharedBox sharedBox) {
        boxMemberRepository.save(boxMemberRepository.save(BoxMember.builder()
                .sharedBox(sharedBox)
                .member(member)
                .role(BoxMemberRole.OWNER)
                .build()));
    }

    @Transactional
    public void addEditorToBox(Member member, SharedBox sharedBox) {
        boxMemberRepository.save(boxMemberRepository.save(BoxMember.builder()
                .sharedBox(sharedBox)
                .member(member)
                .role(BoxMemberRole.EDITOR)
                .build()));
    }

    @Transactional
    public void addViewerToBox(Member member, SharedBox sharedBox) {
        boxMemberRepository.save(boxMemberRepository.save(BoxMember.builder()
                .sharedBox(sharedBox)
                .member(member)
                .role(BoxMemberRole.VIEWER)
                .build()));
    }

    public void validateSharedBoxContentAdder(SharedBox sharedBox, Member member) {
        // 해당 BoxMember 인지
        BoxMember boxMember = boxMemberRepository.findBySharedBoxAndMember(sharedBox, member)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_BOX_MEMBER, member.getMemberId(), "Member"));
        // Role이 충분한지
        if (boxMember.getRole() == BoxMemberRole.VIEWER) {
            throw new CustomException(ErrorCode.FORBIDDEN_BOX_ACCESS, member.getMemberId(), "Member");
        }
    }

    public void validateSharedBoxContentRemover(SharedBox sharedBox, Member member, SharedBoxContent sharedBoxContent) {
        // BoxContent 추가한 회원인지
        if(!sharedBoxContent.getAddedBy().getMemberId().equals(member.getMemberId())) {
            throw new CustomException(ErrorCode.FORBIDDEN_CONTENT_REMOVAL, member.getMemberId(), "Member");
        }
        // 해당 BoxMember 인지
        BoxMember boxMember = boxMemberRepository.findBySharedBoxAndMember(sharedBox, member)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_BOX_MEMBER, member.getMemberId(), "Member"));
        // Role이 충분한지
        if (boxMember.getRole() == BoxMemberRole.VIEWER) {
            throw new CustomException(ErrorCode.FORBIDDEN_BOX_ACCESS, member.getMemberId(), "Member");
        }
    }

    public void validateSharedBoxOwner(SharedBox sharedBox, Member member) {
        // 해당 BoxMember 인지
        BoxMember boxMember = boxMemberRepository.findBySharedBoxAndMember(sharedBox, member)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_BOX_MEMBER, member.getMemberId(), "Member"));
        // Role이 OWNER인지
        if (boxMember.getRole() != BoxMemberRole.OWNER) {
            throw new CustomException(ErrorCode.FORBIDDEN_BOX_ACCESS, member.getMemberId(), "Member");
        }
    }

}
