package net.watchbox.domain.box.service;

import lombok.RequiredArgsConstructor;
import net.watchbox.domain.box.dto.BoxCreateRequest;
import net.watchbox.domain.box.entity.Box;
import net.watchbox.domain.box.entity.BoxType;
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

    public Box findById(Long boxId) {
        return boxRepository.findById(boxId)
                .orElseThrow(() -> new CustomException(ErrorCode.BOX_NOT_FOUND, boxId));
    }

    public List<Box> findAllMyBoxListByOwner(Member owner) {
        return boxRepository.findAllByOwnerAndBoxType(owner, BoxType.MY);
    }


    public List<Box> findAllSharedBoxListByMember(Member member) {
        return boxRepository.findAllByBoxMembers_MemberAndBoxType(member, BoxType.SHARED);
    }

    @Transactional
    public Box createBox(BoxCreateRequest request) {
        return boxRepository.save(Box.builder()
                .name(request.getName())
                .boxType(request.getBoxType())
                .description(request.getDescription())
                .visibleType(request.getVisibleType())
                .build());
    }


    @Transactional
    public void deleteBox(Box box) {
        boxRepository.delete(box);
    }

}
