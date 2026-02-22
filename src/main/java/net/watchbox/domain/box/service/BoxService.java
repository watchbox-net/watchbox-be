package net.watchbox.domain.box.service;

import lombok.RequiredArgsConstructor;
import net.watchbox.domain.box.dto.BoxCreateRequest;
import net.watchbox.domain.box.entity.Box;
import net.watchbox.domain.box.entity.BoxType;
import net.watchbox.domain.box.entity.member.BoxMember;
import net.watchbox.domain.box.repository.BoxMemberRepository;
import net.watchbox.domain.box.repository.BoxRepository;
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

    public Box getByBoxId(Long boxId) {
        return boxRepository.findById(boxId)
                .orElseThrow(() -> new CustomException(ErrorCode.BOX_NOT_FOUND, boxId));
    }

    public List<Box> getAllMyBoxListByOwner(Member owner) {
        return boxRepository.findAllByOwnerAndBoxType(owner, BoxType.MY);
    }

    public List<Box> getAllSharedBoxListByMember(Member member){
        return boxMemberRepository.findWithSharedBoxByMember(member)
                .stream()
                .map(BoxMember::getBox)
                .toList();
    }

    @Transactional
    public Box createBox(Member member, BoxCreateRequest request, BoxType boxType) {
        return boxRepository.save(Box.builder()
                .name(request.getName())
                .boxType(boxType)
                .description(request.getDescription())
                .visibleType(request.getVisibleType())
                .owner(member)
                .build());
    }


    @Transactional
    public void deleteBox(Box box) {
        boxRepository.delete(box);
    }

}
