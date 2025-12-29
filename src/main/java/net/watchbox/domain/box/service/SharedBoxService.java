package net.watchbox.domain.box.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.box.entity.BoxType;
import net.watchbox.domain.box.entity.SharedBox;
import net.watchbox.domain.box.repository.SharedBoxRepository;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.global.dto.response.exception.CustomException;
import net.watchbox.global.dto.response.exception.ErrorCode;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SharedBoxService {
    private final SharedBoxRepository sharedBoxRepository;

    public SharedBox findById(Long boxId) {
        return sharedBoxRepository.findById(boxId)
                .orElseThrow(() -> new CustomException(ErrorCode.SHARED_BOX_NOT_FOUND, boxId));
    }

    @Transactional
    public SharedBox createSharedBox(Member member) {
        return sharedBoxRepository.save(SharedBox.builder()
                .title(member.getNickname() + "의 공유 박스")
                .boxType(BoxType.PRIVATE)
                .build());
    }

    @Transactional
    public void deleteSharedBox(SharedBox sharedBox) {
        sharedBoxRepository.delete(sharedBox);
    }
}
