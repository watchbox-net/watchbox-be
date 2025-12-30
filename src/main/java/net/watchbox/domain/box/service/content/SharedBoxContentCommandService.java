package net.watchbox.domain.box.service.content;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.watchbox.domain.box.entity.SharedBox;
import net.watchbox.domain.box.entity.content.SharedBoxContent;
import net.watchbox.domain.box.repository.content.SharedBoxContentRepository;
import net.watchbox.domain.content.common.entity.Content;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.global.dto.response.exception.CustomException;
import net.watchbox.global.dto.response.exception.ErrorCode;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class SharedBoxContentCommandService {
    private final SharedBoxContentRepository sharedBoxContentRepository;

    // 공유 박스에 컨텐츠 추가
    public void addContentToSharedBox(Member member, SharedBox sharedBox, Content content) {
        SharedBoxContent sharedBoxContent = SharedBoxContent.builder()
                .sharedBox(sharedBox)
                .addedBy(member)
                .content(content)
                .build();
        sharedBoxContentRepository.save(sharedBoxContent);
    }



    // 공유 박스 컨텐츠 삭제
    public void deleteContentFromSharedBox(Member member, SharedBoxContent sharedBoxContent) {
        sharedBoxContentRepository.delete(sharedBoxContent);
    }


    // 공유 박스에 이미 추가된 컨텐츠인지 확인
    public boolean existsInSharedBox(Member member, SharedBox sharedBox, Content content) {
        return sharedBoxContentRepository.existsByAddedByAndSharedBoxAndContent(member, sharedBox, content);
    }

    // 중복 검증 (단일 추가할 때만 검증)
    public void validateNotInSharedBox(Member member, SharedBox sharedBox, Content content) {
        if(existsInSharedBox(member, sharedBox, content)) {
            throw new CustomException(ErrorCode.CONTENT_ALREADY_IN_SHARED_BOX, member.getMemberId());
        }
    }

}
