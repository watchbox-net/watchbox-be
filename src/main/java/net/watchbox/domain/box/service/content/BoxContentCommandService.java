package net.watchbox.domain.box.service.content;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.watchbox.domain.box.entity.Box;
import net.watchbox.domain.box.entity.content.BoxContent;
import net.watchbox.domain.box.repository.content.BoxContentRepository;
import net.watchbox.domain.content.common.entity.Content;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.global.dto.response.exception.CustomException;
import net.watchbox.global.dto.response.exception.ErrorCode;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class BoxContentCommandService {
    private final BoxContentRepository boxContentRepository;

    // 공유 박스에 컨텐츠 추가
    public void addContentToSharedBox(Member member, Box box, Content content) {
        BoxContent boxContent = BoxContent.builder()
                .box(box)
                .addedBy(member)
                .content(content)
                .build();
        boxContentRepository.save(boxContent);
    }



    // 공유 박스 컨텐츠 삭제
    public void deleteContentFromSharedBox(Member member, BoxContent boxContent) {
        boxContentRepository.delete(boxContent);
    }


//    // 해당 멤버로 이미 추가된 컨텐츠인지 확인
//    public boolean existsInSharedBox(Member member, Box box, Content content) {
//        return boxContentRepository.existsByAddedByAndBoxAndContent(member, box, content);
//    }
//
//    // 해당 멤버로 이미 추가된 컨텐츠인지 검증
//    public void validateNotInSharedBox(Member member, Box box, Content content) {
//        if(existsInSharedBox(member, box, content)) {
//            throw new CustomException(ErrorCode.CONTENT_ALREADY_IN_SHARED_BOX, member.getMemberId());
//        }
//    }

}
