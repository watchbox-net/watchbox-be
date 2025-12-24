package net.watchbox.domain.box.service.content;

import lombok.RequiredArgsConstructor;
import net.watchbox.domain.box.entity.content.SharedBoxContent;
import net.watchbox.domain.box.repository.content.SharedBoxContentRepository;
import net.watchbox.global.dto.response.exception.CustomException;
import net.watchbox.global.dto.response.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SharedBoxContentQueryService {
    private final SharedBoxContentRepository sharedBoxContentRepository;

    public SharedBoxContent getSharedBoxContentById(Long sbcId) {
        return sharedBoxContentRepository.findById(sbcId)
                .orElseThrow(() -> new CustomException(ErrorCode.SHARED_BOX_CONTENT_NOT_FOUND, sbcId));
    }

    // 여러개의 공유 박스 ID로 공유 박스 컨텐츠 조회
    public List<SharedBoxContent> getSharedBoxContentsByIds(List<Long> sbcIds) {
        return sharedBoxContentRepository.findAllById(sbcIds);
    }
}
