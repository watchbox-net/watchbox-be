package net.watchbox.domain.box.service.content;

import lombok.RequiredArgsConstructor;
import net.watchbox.domain.box.repository.content.BoxPosterProjection;
import net.watchbox.domain.box.entity.box.Box;
import net.watchbox.domain.box.entity.content.BoxContent;
import net.watchbox.domain.box.repository.content.BoxContentRepository;
import net.watchbox.domain.content.entity.Content;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.global.dto.response.exception.CustomException;
import net.watchbox.global.dto.response.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoxContentQueryService { // find로 전부바꾸기
    private final BoxContentRepository boxContentRepository;

    public BoxContent getByBoxContentId(Long boxContentId) {
        return boxContentRepository.findById(boxContentId)
                .orElseThrow(() -> new CustomException(ErrorCode.BOX_CONTENT_NOT_FOUND, boxContentId));
    }

    public BoxContent getByBoxAndContentOrElseNull(Box box, Content content) {
        return boxContentRepository.findByBoxAndContent(box, content)
                .orElse(null);
    }


    // BoxContent + Content
    public List<BoxContent> getMyBoxContentAllWithSubContent(Box box) {
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

    public List<Long> getBoxIdsContainingContentForMember(Content content, Member member) {
//        return boxContentRepository.findBoxIdsByContent(content);
        return boxContentRepository.findBoxIdsByContentForMember(content, member);
    }
}
