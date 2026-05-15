package net.watchbox.domain.box.service.box;

import lombok.RequiredArgsConstructor;
import net.watchbox.domain.box.dto.box.BoxCreateRequest;
import net.watchbox.domain.box.entity.box.Box;
import net.watchbox.domain.box.entity.box.BoxType;
import net.watchbox.domain.box.entity.box.VisibleType;
import net.watchbox.domain.box.entity.member.BoxMember;
import net.watchbox.domain.box.entity.member.BoxMemberRole;
import net.watchbox.domain.box.repository.member.BoxMemberRepository;
import net.watchbox.domain.box.repository.box.BoxRepository;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.global.dto.response.exception.CustomException;
import net.watchbox.global.dto.response.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoxService {
    private final BoxRepository boxRepository;
    private final BoxMemberRepository boxMemberRepository;

    public Box getByBoxIdOrElseThrow(Long boxId) {
        return boxRepository.findById(boxId)
                .orElseThrow(() -> new CustomException(ErrorCode.BOX_NOT_FOUND, boxId));
    }

    public List<Box> getAllBoxesByMember(Member member) {
        return boxRepository.findAllBoxesByMember(member);
    }


    public List<Box> getAllMyBoxListByOwner(Member owner) {
        return boxRepository.findAllByOwnerAndBoxType(owner, BoxType.MY);
    }

    public List<Box> getAllSharedBoxListByOwner(Member owner) {
        return boxRepository.findAllByOwnerAndBoxType(owner, BoxType.SHARED);
    }

    @Transactional
    public void createInitialMyBox(Member member) {
        Box box = boxRepository.save(Box.builder()
                .name("나의 박스")
                .boxType(BoxType.MY)
                .owner(member)
                .visibleType(VisibleType.PRIVATE)
                .build());
        boxMemberRepository.save(BoxMember.builder()
                .box(box)
                .member(member)
                .role(BoxMemberRole.OWNER)
                .build());
    }

    @Transactional
    public Box createBox(Member member, BoxCreateRequest request) {
        return boxRepository.save(Box.builder()
                .name(request.getName())
                .boxType(request.getBoxType())
                .description(request.getDescription())
                .visibleType(request.getVisibleType())
                .owner(member)
                .build());
    }

    @Transactional
    public void deleteBox(Box box) {
        boxRepository.delete(box);
    }

    @Transactional
    public void deleteAllMyBoxes(Member member) {
        boxRepository.deleteAllByOwnerAndBoxType(member, BoxType.MY);
    }

}
