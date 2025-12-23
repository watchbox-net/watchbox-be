package net.watchbox.domain.box.service.content;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.watchbox.domain.box.repository.content.SharedBoxContentRepository;
import net.watchbox.domain.content.common.entity.Content;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.global.dto.response.exception.CustomException;
import net.watchbox.global.dto.response.exception.ErrorCode;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SharedBoxContentService {
    private final SharedBoxContentRepository sharedBoxContentRepository;

    // 공유 박스에 컨텐츠 추가 - 해당 컨텐츠를 DB에 저장도
    @Transactional
    public void addContentToSharedBox(Member member, Long boxId, Content content) {

    }
    // 공유 박스 컨텐츠 조회
    // 공유 박스 컨텐츠 삭제



    // 중복 검사 (단일 추가할 때만 검증)
    public void validateNotInSharedBox(Member member, Content content) {
        if(sharedBoxContentRepository.existsByAddedByAndContent(member, content)) {
            throw new CustomException(ErrorCode.CONTENT_ALREADY_IN_SHARED_BOX, member.getMemberId());
        }
    }
}
