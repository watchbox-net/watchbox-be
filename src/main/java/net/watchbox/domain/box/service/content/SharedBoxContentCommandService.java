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

import java.util.List;

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
        // 추가한 회원만 삭제 가능
        if(!sharedBoxContent.getAddedBy().equals(member)) {
            throw new CustomException(ErrorCode.FORBIDDEN_BOX_ACCESS, member.getMemberId());
        }
        sharedBoxContentRepository.delete(sharedBoxContent);
    }


    // 공유 박스에 이미 추가된 컨텐츠인지 확인
    public boolean existsInSharedBox(Member member, Content content) {
        return sharedBoxContentRepository.existsByAddedByAndContent(member, content);
    }

    // 중복 검사 (단일 추가할 때만 검증)
    public void validateNotInSharedBox(Member member, Content content) {
        if(sharedBoxContentRepository.existsByAddedByAndContent(member, content)) {
            throw new CustomException(ErrorCode.CONTENT_ALREADY_IN_SHARED_BOX, member.getMemberId());
        }
    }




}
