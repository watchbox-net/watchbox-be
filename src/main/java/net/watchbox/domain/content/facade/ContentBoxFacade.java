package net.watchbox.domain.content.facade;

import lombok.RequiredArgsConstructor;
import net.watchbox.domain.box.entity.box.Box;
import net.watchbox.domain.box.entity.content.BoxContent;
import net.watchbox.domain.box.entity.box.BoxType;
import net.watchbox.domain.box.service.box.BoxService;
import net.watchbox.domain.box.service.content.BoxContentCommandService;
import net.watchbox.domain.box.service.content.BoxContentQueryService;
import net.watchbox.domain.box.service.history.BoxHistoryCommandService;
import net.watchbox.domain.box.service.member.BoxMemberService;
import net.watchbox.domain.box.service.validation.BoxValidator;
import net.watchbox.domain.content.dto.box.ContentBoxDiffRequest;
import net.watchbox.domain.content.dto.box.ContentBoxItem;
import net.watchbox.domain.content.dto.box.ContentBoxSheetResponse;
import net.watchbox.domain.content.dto.box.ContentBoxUpdateResponse;
import net.watchbox.domain.content.entity.MediaType;
import net.watchbox.domain.content.service.ContentCommandService;
import net.watchbox.domain.content.service.ContentQueryService;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.notification.dto.payload.ContentBoxAddedPayload;
import net.watchbox.domain.notification.event.ContentBoxAddedEvent;
import net.watchbox.domain.notification.outbox.OutboxRecorder;
import org.springframework.stereotype.Component;

import net.watchbox.domain.content.entity.Content;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Component
@RequiredArgsConstructor
public class ContentBoxFacade {
    private final ContentQueryService contentQueryService;
    private final ContentCommandService contentCommandService;
    private final BoxService boxService;
    private final BoxContentQueryService boxContentQueryService;
    private final BoxContentCommandService boxContentCommandService;
    private final BoxValidator boxValidator;
    private final BoxMemberService boxMemberService;
    private final BoxHistoryCommandService boxHistoryCommandService;
    private final OutboxRecorder outboxRecorder;

    @Transactional(readOnly = true)
    public ContentBoxSheetResponse getContentBoxSheet(Member member, Long tmdbId, MediaType mediaType) {
        List<Box> boxList = boxService.getAllBoxesByMember(member);
        Map<Long, List<String>> posterMap = boxContentQueryService.getRecentPosterPathsByBoxes(boxList);

        /*
        Content가 DB에 있으면 contentId로 박스 포함 여부 조회,
        없으면 어떤 박스에도 없으니 빈 리스트
        */
        Optional<Content> contentOpt = contentQueryService.findByTmdbIdAndMediaType(tmdbId, mediaType);
        List<Long> boxIdsWithContent = contentOpt
                .map(content -> boxContentQueryService.getBoxIdsContainingContentForMember(member, content))
                .orElse(Collections.emptyList());

        List<ContentBoxItem> contentBoxItemList = boxList.stream()
                .map(box -> ContentBoxItem.of(box,
                        posterMap.getOrDefault(box.getBoxId(), Collections.emptyList()),
                        boxIdsWithContent.contains(box.getBoxId())))
                .toList();

        return ContentBoxSheetResponse.builder()
                .contentBoxItemList(contentBoxItemList)
                .totalCount((long) contentBoxItemList.size())
                .build();
    }

    @Transactional
    public ContentBoxUpdateResponse updateContentBoxes(Member member, Long tmdbId, MediaType mediaType, ContentBoxDiffRequest request) {
        // 1. Content 확보 (DB에 없으면 TMDB에서 가져와 저장)
        Content content = contentCommandService.getOrSaveContentCascade(tmdbId, mediaType);

        // 2. addBoxIds 처리
        List<Long> addedBoxIds = new ArrayList<>();
        for (Long boxId : request.getAddBoxIds()) {
            Box box = boxService.getByBoxIdOrElseThrow(boxId);
            boxValidator.validateBoxContentAdder(box, member); // 권한 검증
            if (boxValidator.contentExistsInBoxByMember(member, box, content)) {
                continue; // 해당 멤버로 추가된 콘텐츠이면 skip
            }
            boxContentCommandService.addContentToBox(member, box, content);
            addedBoxIds.add(boxId);

            // 박스 히스토리 기록
            boxHistoryCommandService.contentAdded(box, member, content);

            // 알림 이벤트를 outbox 에 기록 (추가 시에만) — 이 트랜잭션과 원자적으로 묶인다.
            // - SHARED 박스: publisher 제외한 멤버 전원에게 알림
            // - MY 박스: 본인 활동이라 알림 불필요 → 스킵
            if (box.getBoxType() == BoxType.SHARED) {
                List<Long> receiverIds = boxMemberService.getAllBoxMemberIdsExcluding(box, member);
                if (!receiverIds.isEmpty()) {
                    outboxRecorder.record(new ContentBoxAddedEvent(
                            receiverIds,
                            ContentBoxAddedPayload.of(content, box, member)
                    ));
                }
            }
        }

        // 3. removeBoxIds 처리
        List<Long> removedBoxIds = new ArrayList<>();
        for (Long boxId : request.getRemoveBoxIds()) {
            Box box = boxService.getByBoxIdOrElseThrow(boxId);
            // publisher 까지 넣어 찾는다. 공유 박스는 같은 콘텐츠를 여러 멤버가 각자 담을 수 있어
            // (box, content) 만으로는 행이 유일하지 않고, 둘 이상이면 단건 조회가 예외로 끝난다.
            // 그 예외는 아래 권한 검증보다 먼저 터져서, 정당하게 담은 사람도 자기 것을 못 빼게 된다.
            BoxContent boxContent =
                    boxContentQueryService.getByBoxAndContentAndPublisherOrElseNull(box, content, member);
            if (boxContent == null) continue;  // 안 담았거나 남이 담은 것 — 건너뛴다
            boxContentCommandService.deleteContentFromBox(boxContent);
            removedBoxIds.add(boxId);

            // 박스 히스토리 기록
            boxHistoryCommandService.contentDeleted(box, member, content);
        }

        // 4. 응답
        return ContentBoxUpdateResponse.builder()
                .memberId(member.getMemberId())
                .contentId(content.getContentId())
                .addedBoxIds(addedBoxIds)
                .removedBoxIds(removedBoxIds)
                .build();
    }

}
