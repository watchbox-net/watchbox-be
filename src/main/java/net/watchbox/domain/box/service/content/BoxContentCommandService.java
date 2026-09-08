package net.watchbox.domain.box.service.content;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.watchbox.domain.box.entity.box.Box;
import net.watchbox.domain.box.entity.content.BoxContent;
import net.watchbox.domain.box.repository.content.BoxContentRepository;
import net.watchbox.domain.content.entity.Content;
import net.watchbox.domain.member.entity.Member;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class BoxContentCommandService {
    private final BoxContentRepository boxContentRepository;

    // 박스에 콘텐츠 추가
    public BoxContent addContentToBox(Member member, Box box, Content content) {
        BoxContent boxContent = boxContentRepository.save(BoxContent.builder()
                .box(box)
                .publisher(member)
                .content(content)
                .mediaType(content.getMediaType())
                .build());
        box.updateLastContentAddedAt(boxContent.getCreatedAt());
        return boxContent;
    }

    // 박스 컨텍츠 삭제
    public void deleteContentFromBox(BoxContent boxContent) {
        boxContentRepository.delete(boxContent);
    }

    public void deleteAllByBox(Box box) {
        boxContentRepository.deleteAllByBox(box);
    }

    public void deleteAllBoxContentByMember(Member member) {
        boxContentRepository.deleteAllByPublisher(member);
    }

//    // 해당 멤버로 이미 추가된 콘텐츠인지 확인
//    public boolean existsInSharedBox(Member member, Box box, Content content) {
//        return boxContentRepository.existsByAddedByAndBoxAndContent(member, box, content);
//    }
//
//    // 해당 멤버로 이미 추가된 콘텐츠인지 검증
//    public void validateNotInSharedBox(Member member, Box box, Content content) {
//        if(existsInSharedBox(member, box, content)) {
//            throw new CustomException(ErrorCode.CONTENT_ALREADY_IN_SHARED_BOX, member.getMemberId());
//        }
//    }

}
