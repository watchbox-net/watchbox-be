package net.watchbox.domain.box.service.member;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.box.entity.box.Box;
import net.watchbox.domain.box.entity.member.BoxMember;
import net.watchbox.domain.box.entity.member.BoxMemberRole;
import net.watchbox.domain.box.repository.member.BoxMemberRepository;
import net.watchbox.domain.member.entity.Member;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BoxMemberService {
    private final BoxMemberRepository boxMemberRepository;

    public List<BoxMember> findAllBySharedBox(Box box) {
        return boxMemberRepository.findAllByBox(box);
    }

    public List<BoxMember> findAllByMember(Member member) {
        return boxMemberRepository.findAllByMember(member);
    }

    @Transactional
    public void addOwnerToBox(Member member, Box box) {
        boxMemberRepository.save(boxMemberRepository.save(BoxMember.builder()
                .box(box)
                .member(member)
                .role(BoxMemberRole.OWNER)
                .build()));
    }

    @Transactional
    public void addEditorToBox(Member member, Box box) {
        boxMemberRepository.save(boxMemberRepository.save(BoxMember.builder()
                .box(box)
                .member(member)
                .role(BoxMemberRole.EDITOR)
                .build()));
    }

    @Transactional
    public void addViewerToBox(Member member, Box box) {
        boxMemberRepository.save(boxMemberRepository.save(BoxMember.builder()
                .box(box)
                .member(member)
                .role(BoxMemberRole.VIEWER)
                .build()));
    }

//    public void validateBoxContentAdder(Box box, Member member) {
//        // 해당 BoxMember 인지
//        BoxMember boxMember = boxMemberRepository.findByBoxAndMember(box, member)
//                .orElseThrow(() -> new CustomException(ErrorCode.NOT_BOX_MEMBER, member.getMemberId(), "Member"));
//        // Role이 충분한지
//        if (boxMember.getRole() == BoxMemberRole.VIEWER) {
//            throw new CustomException(ErrorCode.FORBIDDEN_BOX_ACCESS, member.getMemberId(), "Member");
//        }
//    }
//
//    public void validateBoxContentRemover(Box box, Member member, BoxContent boxContent) {
//        // BoxContent 추가한 멤버인지
//        if(!boxContent.getAddedBy().getMemberId().equals(member.getMemberId())) {
//            throw new CustomException(ErrorCode.FORBIDDEN_CONTENT_REMOVAL, member.getMemberId(), "Member");
//        }
//        // 해당 BoxMember 인지
//        BoxMember boxMember = boxMemberRepository.findByBoxAndMember(box, member)
//                .orElseThrow(() -> new CustomException(ErrorCode.NOT_BOX_MEMBER, member.getMemberId(), "Member"));
//        // Role이 충분한지
//        if (boxMember.getRole() == BoxMemberRole.VIEWER) {
//            throw new CustomException(ErrorCode.FORBIDDEN_BOX_ACCESS, member.getMemberId(), "Member");
//        }
//    }
//
//    public void validateBoxOwner(Box box, Member member) {
//        // 해당 BoxMember 인지
//        BoxMember boxMember = boxMemberRepository.findByBoxAndMember(box, member)
//                .orElseThrow(() -> new CustomException(ErrorCode.NOT_BOX_MEMBER, member.getMemberId(), "Member"));
//        // Role이 OWNER인지
//        if (boxMember.getRole() != BoxMemberRole.OWNER) {
//            throw new CustomException(ErrorCode.FORBIDDEN_BOX_ACCESS, member.getMemberId(), "Member");
//        }
//    }
//
//    // 공유 박스의 기존 멤버인지 검증
//    public void validateExistingBoxMember(Box box, Member member) {
//        if(boxMemberRepository.existsByBoxAndMember(box, member)) {
//            throw new CustomException(ErrorCode.ALREADY_BOX_MEMBER, member.getMemberId(), "Member");
//        }
//    }
}
