package net.watchbox.domain.box.service.content;

import lombok.RequiredArgsConstructor;
import net.watchbox.domain.box.dto.content.BoxContentRecordQueryRequest;
import net.watchbox.domain.box.repository.content.BoxContentQueryRepository;
import net.watchbox.domain.box.repository.content.BoxPosterProjection;
import net.watchbox.domain.box.entity.box.Box;
import net.watchbox.domain.box.entity.content.BoxContent;
import net.watchbox.domain.box.repository.content.BoxContentRepository;
import net.watchbox.domain.content.entity.Content;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.global.dto.CursorPayload;
import net.watchbox.global.dto.response.exception.CustomException;
import net.watchbox.global.dto.response.exception.ErrorCode;
import net.watchbox.global.util.CursorCodec;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoxContentQueryService { // find로 전부바꾸기
    private final BoxContentRepository boxContentRepository;
    private final BoxContentQueryRepository boxContentQueryRepository;
    private final CursorCodec cursorCodec;

    // BoxContent 동적 조회 (필터 + 정렬 + 커서 페이지네이션)
    public List<BoxContent> getBoxContentList(Box box, Member member, BoxContentRecordQueryRequest request, int size) {
        CursorPayload cursor = cursorCodec.decode(request.getCursor()); // null 이면 첫 페이지
        return boxContentQueryRepository.findBoxContentList(box, member, request, cursor, size);
    }

    public Long countByBox(Box box) {
        return boxContentRepository.countByBox(box);
    }

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

    // Content가 포함된 Member의 모든 박스 ID 목록 조회
    public List<Long> getBoxIdsContainingContentForMember(Member member, Content content) {
        return boxContentRepository.findBoxIdsByContentForMember(content, member);
    }

    // Content가 멤버의 박스에 있는지 유무
    public boolean existsBoxContainingContentForMember(Member member, Content content) {
        return boxContentRepository.existsByPublisherAndContent(member, content);
    }
}
