package net.watchbox.domain.box.service.content;

import lombok.RequiredArgsConstructor;
import net.watchbox.domain.box.dto.response.content.AdderItem;
import net.watchbox.domain.box.dto.response.content.ContentItem;
import net.watchbox.domain.box.dto.response.content.SharedBoxContentItem;
import net.watchbox.domain.box.dto.response.content.SharedBoxContentResponse;
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

    public SharedBoxContentResponse getSharedBoxContentsAll(SharedBox sharedBox, Member member) {
        Long totalCount = sharedBoxContentRepository.countBySharedBox(sharedBox);
        List<SharedBoxContent> sharedBoxContents = sharedBoxContentRepository.findAllBySharedBox(sharedBox);

        List<SharedBoxContentItem> sharedBoxContentItems = sharedBoxContents.stream()
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
                                    ContentItem.fromMovie(content.getMovie()), sbc.getSharedBoxContentId(), List.of(adderItem)
                            );
                        }
                        case TV -> {
                            return new SharedBoxContentItem(
                                    ContentItem.fromTv(content.getTv()), sbc.getSharedBoxContentId(), List.of(adderItem)
                            );
                        }
                        case PERSON -> {
                            return new SharedBoxContentItem(
                                    ContentItem.fromPerson(content.getPerson()), sbc.getSharedBoxContentId(), List.of(adderItem)
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
