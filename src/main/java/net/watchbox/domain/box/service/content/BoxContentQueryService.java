package net.watchbox.domain.box.service.content;

import lombok.RequiredArgsConstructor;
import net.watchbox.domain.box.repository.content.BoxPosterProjection;
import net.watchbox.domain.box.entity.box.Box;
import net.watchbox.domain.box.entity.content.BoxContent;
import net.watchbox.domain.box.repository.content.BoxContentRepository;
import net.watchbox.domain.content.base.entity.Content;
import net.watchbox.global.dto.response.exception.CustomException;
import net.watchbox.global.dto.response.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoxContentQueryService { // find로 전부바꾸기
    private final BoxContentRepository boxContentRepository;

    public BoxContent getByBoxContentId(Long boxContentId) {
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

//    public SharedBoxContentResponse getSharedBoxContentsAll(Box box, Member member) {
//        Long totalCount = boxContentRepository.countByBox(box);
//        List<BoxContent> boxContents = boxContentRepository.findAllByBox(box);
//
//        List<SharedBoxContentItem> sharedBoxContentItems = boxContents.stream()
//                .map(sbc -> {
//                    Content content = sbc.getContent();
//                    Member addedBy = sbc.getAddedBy();
//                    AdderItem adderItem = AdderItem.builder()
//                            .adderid(addedBy.getMemberId())
//                            .nickname(addedBy.getNickname())
//                            .profileImage(addedBy.getProfileImage())
//                            .build();
//                    switch(content.getMediaType()){
//                        case MOVIE -> {
//                            return new SharedBoxContentItem(
//                                    ContentSummary.fromMovie(content.getMovie()), sbc.getBoxContentId(), List.of(adderItem)
//                            );
//                        }
//                        case TV -> {
//                            return new SharedBoxContentItem(
//                                    ContentSummary.fromTv(content.getTv()), sbc.getBoxContentId(), List.of(adderItem)
//                            );
//                        }
//                        case PERSON -> {
//                            return new SharedBoxContentItem(
//                                    ContentSummary.fromPerson(content.getPerson()), sbc.getBoxContentId(), List.of(adderItem)
//                            );
//                        }
//                        default -> throw new CustomException(ErrorCode.UNSUPPORTED_MEDIA_TYPE);
//                    }
//                })
//                .toList();
//        // ToDo: SharedBoxContent에 여러명이 넣었을 경우 하나로 합쳐서 SharedBoxContentItem 응답하는 로직 필요
//
//        return new SharedBoxContentResponse(totalCount, sharedBoxContentItems);
//    }

    public List<BoxContent> getAllByMyBox(Box box) {
        return boxContentRepository.findAllByBox(box);
    }

    // BoxContent + Content
    public List<BoxContent> getMyBoxContentAllWithSubContent(Box box) {
        return boxContentRepository.findAllWithSubContentByBox(box);
    }

    public List<BoxContent> getSharedBoxContentAllWithSubContent(Box box) {
        return boxContentRepository.findAllWithSubContentByBox(box);
    }

    // 박스별 최근 포스터 경로 3개를 Map<boxId, List<posterPath>>로 반환
    public Map<Long, List<String>> getRecentPosterPathsByBoxes(List<Box> boxes) {
        if (boxes.isEmpty()) {
            return Collections.emptyMap();
        }
        List<BoxPosterProjection> boxPosters = boxContentRepository.findRecentPostersByBoxes(boxes);

        return boxPosters.stream()
                .collect(Collectors.groupingBy(
                        BoxPosterProjection::getBoxId,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> list.stream()
                                        .map(BoxPosterProjection::getPosterPath)
                                        .limit(3)
                                        .toList()
                        )
                ));
    }

}
