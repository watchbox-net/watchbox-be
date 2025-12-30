package net.watchbox.domain.box.service.content;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.box.dto.response.content.ContentItem;
import net.watchbox.domain.box.dto.response.my.MyBoxContentItem;
import net.watchbox.domain.box.dto.response.my.MyBoxContentResponse;
import net.watchbox.domain.box.entity.content.MyBoxContent;
import net.watchbox.domain.box.repository.content.MyBoxContentRepository;
import net.watchbox.domain.content.common.entity.Content;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.global.dto.response.exception.CustomException;
import net.watchbox.global.dto.response.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MyBoxContentService {
    private final MyBoxContentRepository myBoxContentRepository;

    // 내 박스에 콘텐츠 추가
    @Transactional
    public void addContentToMyBox(Member member, Content content) {
        myBoxContentRepository.save(MyBoxContent.builder()
                .member(member)
                .content(content)
                .build());
    }

    // 내 박스 콘텐츠 전체 조회
    public MyBoxContentResponse getMyBoxContentsAll(Member member) {
        Long totalCount = myBoxContentRepository.countByMember(member);
        List<MyBoxContent> myBoxContents = myBoxContentRepository.findByMemberOrderByCreatedAtDesc(member);
        List<MyBoxContentItem> myBoxContentItems = myBoxContents.stream()
                .map(myBoxContent -> {
//                    Content content = contentQueryService.findContentById(myBoxContent.getContent().getTmdbId())
//                            .orElseThrow(() -> new CustomException(ErrorCode.CONTENT_NOT_FOUND));
                    Content content = myBoxContent.getContent();
                    switch (content.getMediaType()) {
                        case MOVIE -> {
                            return new MyBoxContentItem(ContentItem.fromMovie(content.getMovie()), myBoxContent.getMyBoxContentId());
                        }
                        case TV -> {
                            return new MyBoxContentItem(ContentItem.fromTv(content.getTv()), myBoxContent.getMyBoxContentId());
                        }
                        case PERSON -> {
                            return new MyBoxContentItem(ContentItem.fromPerson(content.getPerson()), myBoxContent.getMyBoxContentId());
                        }
                        default -> throw new CustomException(ErrorCode.UNSUPPORTED_MEDIA_TYPE);
                    }
                })
                .toList();
        return new MyBoxContentResponse(totalCount, myBoxContentItems);
    }

    @Transactional
    public void deleteContentFromMyBox(Member member, Long tmdbId) {
        MyBoxContent myBoxContent = myBoxContentRepository.findByMemberAndContent_TmdbId(member, tmdbId)
                .orElseThrow(() -> new CustomException(ErrorCode.CONTENT_NOT_IN_MY_BOX));
        myBoxContentRepository.delete(myBoxContent);
        log.info("My Box에서 콘텐츠 삭제 완료. memberId = {}, tmdbId = {}", member.getMemberId(), tmdbId);
    }

    // 중복 검사 (단일 추가할 때만 검증)
    public void validateNotInMyBox(Member member, Content content) {
        if(myBoxContentRepository.existsByMemberAndContent(member, content)) {
            throw new CustomException(ErrorCode.CONTENT_ALREADY_IN_MY_BOX);
        }
    }
}
