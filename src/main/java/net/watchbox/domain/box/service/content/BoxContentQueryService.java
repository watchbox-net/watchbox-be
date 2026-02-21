package net.watchbox.domain.box.service.content;

import lombok.RequiredArgsConstructor;
import net.watchbox.domain.box.dto.response.content.AdderItem;
import net.watchbox.domain.box.dto.response.content.ContentSummary;
import net.watchbox.domain.box.dto.response.content.SharedBoxContentItem;
import net.watchbox.domain.box.dto.response.content.SharedBoxContentResponse;
import net.watchbox.domain.box.entity.Box;
import net.watchbox.domain.box.entity.content.BoxContent;
import net.watchbox.domain.box.repository.content.BoxContentRepository;
import net.watchbox.domain.content.common.entity.Content;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.global.dto.response.exception.CustomException;
import net.watchbox.global.dto.response.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoxContentQueryService { // find로 전부바꾸기
    private final BoxContentRepository boxContentRepository;

    public BoxContent findById(Long boxContentId) {
        return boxContentRepository.findById(boxContentId)
                .orElseThrow(() -> new CustomException(ErrorCode.BOX_CONTENT_NOT_FOUND, boxContentId));
    }

    public BoxContent getMyBoxContentByBoxAndContent(Box box, Content content) {
        return boxContentRepository.findByBoxAndContent(box, content)
                .orElseThrow(() -> new CustomException(ErrorCode.BOX_CONTENT_NOT_FOUND, content.getTmdbId()));
    }

    public BoxContent getSharedBoxContentById(Long sbcId) {
        return boxContentRepository.findById(sbcId)
                .orElseThrow(() -> new CustomException(ErrorCode.SHARED_BOX_CONTENT_NOT_FOUND, sbcId));
    }

    // 여러개의 공유 박스 ID로 공유 박스 컨텐츠 조회
    public List<BoxContent> getSharedBoxContentsByIds(List<Long> sbcIds) {
        return boxContentRepository.findAllById(sbcIds);
    }

    public SharedBoxContentResponse getSharedBoxContentsAll(Box box, Member member) {
        Long totalCount = boxContentRepository.countByBox(box);
        List<BoxContent> boxContents = boxContentRepository.findAllByBox(box);

        List<SharedBoxContentItem> sharedBoxContentItems = boxContents.stream()
                .map(sbc -> {
                    Content content = sbc.getContent();
                    Member addedBy = sbc.getAddedBy();
                    AdderItem adderItem = AdderItem.builder()
                            .id(addedBy.getMemberId())
                            .nickname(addedBy.getNickname())
                            .profileImage(addedBy.getProfileImage())
                            .build();
                    switch(content.getMediaType()){
                        case MOVIE -> {
                            return new SharedBoxContentItem(
                                    ContentSummary.fromMovie(content.getMovie()), sbc.getBoxContentId(), List.of(adderItem)
                            );
                        }
                        case TV -> {
                            return new SharedBoxContentItem(
                                    ContentSummary.fromTv(content.getTv()), sbc.getBoxContentId(), List.of(adderItem)
                            );
                        }
                        case PERSON -> {
                            return new SharedBoxContentItem(
                                    ContentSummary.fromPerson(content.getPerson()), sbc.getBoxContentId(), List.of(adderItem)
                            );
                        }
                        default -> throw new CustomException(ErrorCode.UNSUPPORTED_MEDIA_TYPE);
                    }
                })
                .toList();
        // ToDo: SharedBoxContent에 여러명이 넣었을 경우 하나로 합쳐서 SharedBoxContentItem 응답하는 로직 필요

        return new SharedBoxContentResponse(totalCount, sharedBoxContentItems);
    }
}
