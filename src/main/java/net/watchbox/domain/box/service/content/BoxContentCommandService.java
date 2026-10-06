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

    /**
     * 박스를 나가는 멤버가 담았던 콘텐츠 정리.
     *
     * <p>남겨두면 <b>아무도 지울 수 없는 콘텐츠</b>가 된다 — 삭제 권한은 담은 본인에게만 있는데
     * ({@code validateBoxContentRemover}) 그 사람이 더는 박스 멤버가 아니기 때문이다.
     * 회원 탈퇴({@code MemberFacade.deleteMember})가 담은 콘텐츠를 모두 지우는 것과 같은 규칙이다.
     */
    public void deleteAllByPublisherAndBox(Member publisher, Box box) {
        boxContentRepository.deleteAllByPublisherAndBox(publisher, box);
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
