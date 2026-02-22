package net.watchbox.domain.box.service;

import lombok.RequiredArgsConstructor;
import net.watchbox.domain.box.entity.Box;
import net.watchbox.domain.box.entity.content.BoxContent;
import net.watchbox.domain.box.entity.member.BoxMember;
import net.watchbox.domain.box.entity.member.BoxMemberRole;
import net.watchbox.domain.box.repository.BoxMemberRepository;
import net.watchbox.domain.box.repository.content.BoxContentRepository;
import net.watchbox.domain.content.common.entity.Content;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.global.dto.response.exception.CustomException;
import net.watchbox.global.dto.response.exception.ErrorCode;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BoxValidator {
    private final BoxContentRepository boxContentRepository;
    private final BoxMemberRepository boxMemberRepository;

    // ToDo: 접근 상황별로 예외 멘트 다르게 나오도록 나눠야함
    // ------------------- Box CRUD 권한 검증 -------------------

    // 해당 BoxMember인지 검증
    public void validateBoxMember(Box box, Member member) {
        if(!boxMemberRepository.existsByBoxAndMember(box, member)) {
            throw new CustomException(ErrorCode.NOT_BOX_MEMBER, member.getMemberId(), "Member");
        }
    }

    public void validateBoxOwner(Box box, Member member) {
        // BoxMember 조회
        BoxMember boxMember = boxMemberRepository.findByBoxAndMember(box, member)
                .orElseThrow(() -> new CustomException(ErrorCode.BOX_MEMBER_NOT_FOUND, member.getMemberId(), "Member"));
        // Role이 OWNER 인지
        if (boxMember.getRole() != BoxMemberRole.OWNER) {
            throw new CustomException(ErrorCode.FORBIDDEN_BOX_ACCESS, member.getMemberId(), "Member");
        }
    }

    public void validateBoxEditor(Box box, Member member) {
        // BoxMember 조회
        BoxMember boxMember = boxMemberRepository.findByBoxAndMember(box, member)
                .orElseThrow(() -> new CustomException(ErrorCode.BOX_MEMBER_NOT_FOUND, member.getMemberId(), "Member"));
        // Role이 OWNER 또는 EDITOR 인지
        if (boxMember.getRole() != BoxMemberRole.OWNER && boxMember.getRole() != BoxMemberRole.EDITOR) {
            throw new CustomException(ErrorCode.FORBIDDEN_BOX_ACCESS, member.getMemberId(), "Member");
        }
    }

    // ------------------- InviteBoxRequest 검증 -------------------

    // 공유 박스의 기존 멤버로 이미 존재하는지 검증
    public void validateExistingBoxMember(Box box, Member member) {
        if(boxMemberRepository.existsByBoxAndMember(box, member)) {
            throw new CustomException(ErrorCode.ALREADY_BOX_MEMBER, member.getMemberId(), "Member");
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
            throw new CustomException(ErrorCode.BOX_CONTENT_ALREADY_IN_BOX);
        }
    }

    // 해당 멤버로 이미 추가된 컨텐츠인지 확인
    public boolean contentExistsInSharedBox(Member member, Box box, Content content) {
        return boxContentRepository.existsByPublisherAndBoxAndContent(member, box, content);
    }

    // 해당 멤버로 이미 추가된 컨텐츠인지 검증
    public void validateContentNotInSharedBox(Member member, Box box, Content content) {
        if(contentExistsInSharedBox(member, box, content)) {
            throw new CustomException(ErrorCode.CONTENT_ALREADY_IN_SHARED_BOX, member.getMemberId());
        }
    }

    // ------------------- BoxContent 추가/삭제 권한 검증 -------------------

    public void validateBoxContentAdder(Box box, Member member) {
        // BoxMember 조회
        BoxMember boxMember = boxMemberRepository.findByBoxAndMember(box, member)
                .orElseThrow(() -> new CustomException(ErrorCode.BOX_MEMBER_NOT_FOUND, member.getMemberId(), "Member"));
        // Role이 충분한지
        if (boxMember.getRole() == BoxMemberRole.VIEWER) {
            throw new CustomException(ErrorCode.INSUFFICIENT_BOX_CONTENT_EDIT_PERMISSION, member.getMemberId(), "Member");
        }
    }

    public void validateBoxContentRemover(Member member, BoxContent boxContent) {
        // BoxContent 추가한 멤버인지
        if(!boxContent.getPublisher().getMemberId().equals(member.getMemberId())) {
            throw new CustomException(ErrorCode.FORBIDDEN_CONTENT_REMOVAL, member.getMemberId(), "Member");
        }
    }

}
