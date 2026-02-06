package net.watchbox.domain.box.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.box.entity.BoxType;
import net.watchbox.domain.box.entity.Box;
import net.watchbox.domain.box.repository.BoxRepository;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.global.dto.response.exception.CustomException;
import net.watchbox.global.dto.response.exception.ErrorCode;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SharedBoxService {
    private final BoxRepository boxRepository;

    public Box findById(Long boxId) {
        return boxRepository.findById(boxId)
                .orElseThrow(() -> new CustomException(ErrorCode.SHARED_BOX_NOT_FOUND, boxId));
    }


    @Transactional
    public Box createSharedBox(Member member) {
        return boxRepository.save(Box.builder()
                .title(member.getNickname() + "의 공유 박스")
                .boxType(BoxType.SHARED)
                .build());
    }

    @Transactional
    public void deleteSharedBox(Box box) {
        boxRepository.delete(box);
    }

}
