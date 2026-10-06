package net.watchbox.domain.box.facade.record;

import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import net.watchbox.domain.box.dto.record.request.BoxRecordDiffRequest;
import net.watchbox.domain.box.dto.record.request.BoxRecordSheetQueryRequest;
import net.watchbox.domain.box.dto.record.response.BoxRecordUpdateResponse;
import net.watchbox.domain.box.entity.box.Box;
import net.watchbox.domain.box.entity.box.BoxType;
import net.watchbox.domain.box.entity.content.BoxContent;
import net.watchbox.domain.box.service.box.BoxService;
import net.watchbox.domain.box.service.content.BoxContentCommandService;
import net.watchbox.domain.box.service.content.BoxContentQueryService;
import net.watchbox.domain.box.service.history.BoxHistoryCommandService;
import net.watchbox.domain.box.service.member.BoxMemberService;
import net.watchbox.domain.box.service.validation.BoxValidator;
import net.watchbox.domain.content.dto.list.ContentCursorPageResponse;
import net.watchbox.domain.content.entity.Content;
import net.watchbox.domain.content.mapper.record.ContentRecordMapper;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.notification.dto.payload.ContentBoxAddedPayload;
import net.watchbox.domain.notification.event.ContentBoxAddedEvent;
import net.watchbox.domain.notification.outbox.OutboxRecorder;
import net.watchbox.domain.record.dto.record.request.ContentRecordQueryRequest;
import net.watchbox.domain.record.entity.record.ContentRecord;
import net.watchbox.domain.record.repository.record.ContentRecordCursorBuilder;
import net.watchbox.domain.record.service.record.ContentRecordQueryService;
import net.watchbox.global.constants.AppConstants;
import net.watchbox.global.util.CursorCodec;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 시청 기록에서 골라 박스에 담는 화면("시청 기록에서 박스에 추가하기").
 *
 * <p>{@code ContentBoxFacade}(콘텐츠 하나 → 여러 박스)의 <b>거울</b>이다. 이쪽은 박스 하나를
 * 고정해놓고 시청 기록을 넘기며 체크한다. 포함 판정·추가 권한·삭제 권한 규칙은 양쪽이 같아야
 * 해서 같은 {@code BoxValidator} 를 쓴다.
 *
 * <p><b>이 화면에서 시청 기록은 바뀌지 않는다.</b> 조회만 하고, 쓰기는 BoxContent 쪽에만 일어난다.
 */
@Component
@RequiredArgsConstructor
@Observed
public class BoxRecordSheetFacade {

    private final BoxService boxService;
    private final BoxValidator boxValidator;
    private final BoxContentQueryService boxContentQueryService;
    private final BoxContentCommandService boxContentCommandService;
    private final BoxHistoryCommandService boxHistoryCommandService;
    private final BoxMemberService boxMemberService;
    private final ContentRecordQueryService contentRecordQueryService;
    private final OutboxRecorder outboxRecorder;
    private final CursorCodec cursorCodec;

    /**
     * 내 시청 기록 한 페이지 + 각 콘텐츠의 이 박스 포함 여부.
     *
     * <p>포함 여부는 페이지의 contentId 를 모아 <b>쿼리 한 번</b>으로 받는다. 행마다 확인하면
     * 페이지 크기만큼 쿼리가 늘어난다.
     */
    @Transactional(readOnly = true)
    public ContentCursorPageResponse getRecordSheet(Member member, Long boxId,
                                                    BoxRecordSheetQueryRequest request) {
        Box box = boxService.getByBoxIdOrElseThrow(boxId);
        boxValidator.validateBoxMember(box, member);

        // 조회와 커서 생성이 같은 인스턴스를 봐야 정렬 키와 커서 구조가 어긋나지 않는다.
        ContentRecordQueryRequest query = request.toContentRecordQuery();

        List<ContentRecord> fetched =
                contentRecordQueryService.getMyContentRecordList(member, query, AppConstants.PAGE_SIZE);

        boolean hasNext = fetched.size() > AppConstants.PAGE_SIZE;
        List<ContentRecord> page = hasNext ? fetched.subList(0, AppConstants.PAGE_SIZE) : fetched;

        String nextCursor = hasNext
                ? cursorCodec.encode(ContentRecordCursorBuilder.build(page.getLast(), query))
                : null;

        List<Long> contentIds = page.stream()
                .map(record -> record.getContent().getContentId())
                .toList();
        Set<Long> addedContentIds =
                new HashSet<>(boxContentQueryService.getContentIdsInBoxForMember(box, contentIds, member));

        return ContentCursorPageResponse.builder()
                .contentItemList(ContentRecordMapper.toContentItems(page, addedContentIds))
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .build();
    }

    /**
     * 체크 상태 변경분을 박스에 반영한다.
     *
     * <p>이미 담긴 것을 또 추가하거나 내가 담지 않은 것을 삭제 요청하면 <b>예외 없이 건너뛴다.</b>
     * 여러 건을 한 번에 보내는 화면이라, 하나 어긋났다고 전체를 되돌리면 사용자는 무엇이
     * 반영됐는지 알 수 없다. 실제로 반영된 것만 응답에 담아 프론트가 체크 상태를 맞추게 한다.
     */
    @Transactional
    public BoxRecordUpdateResponse updateBoxRecords(Member member, Long boxId,
                                                    BoxRecordDiffRequest request) {
        Box box = boxService.getByBoxIdOrElseThrow(boxId);

        List<Long> addIds = request.addContentIdsOrEmpty();
        List<Long> removeIds = request.removeContentIdsOrEmpty();

        // 이 화면의 출처가 내 시청 기록이므로, 다루는 콘텐츠도 거기로 한정한다.
        // 기록에 없는 contentId 가 섞여 오면 조회 단계에서 걸러진다.
        Map<Long, Content> contentById = resolveFromMyRecords(member, addIds, removeIds);

        List<Long> addedContentIds = addContents(member, box, addIds, contentById);
        List<Long> removedContentIds = removeContents(member, box, removeIds, contentById);

        return BoxRecordUpdateResponse.builder()
                .boxId(box.getBoxId())
                .memberId(member.getMemberId())
                .addedContentIds(addedContentIds)
                .removedContentIds(removedContentIds)
                .build();
    }

    private Map<Long, Content> resolveFromMyRecords(Member member, List<Long> addIds, List<Long> removeIds) {
        Set<Long> requested = new LinkedHashSet<>(addIds);
        requested.addAll(removeIds);
        if (requested.isEmpty()) {
            return Map.of();
        }
        return contentRecordQueryService.getByMemberAndContentIds(member, List.copyOf(requested)).stream()
                .map(ContentRecord::getContent)
                .collect(Collectors.toMap(Content::getContentId, Function.identity(), (a, b) -> a));
    }

    private List<Long> addContents(Member member, Box box, List<Long> addIds, Map<Long, Content> contentById) {
        if (addIds.isEmpty()) {
            return List.of();
        }
        // 박스 단위 권한이라 콘텐츠마다 확인할 필요가 없다. 삭제만 하는 요청이면 여기까지 오지 않는다.
        boxValidator.validateBoxContentAdder(box, member);

        List<Long> added = new ArrayList<>();
        for (Long contentId : addIds) {
            Content content = contentById.get(contentId);
            if (content == null || boxValidator.contentExistsInBoxByMember(member, box, content)) {
                continue;
            }
            boxContentCommandService.addContentToBox(member, box, content);
            boxHistoryCommandService.contentAdded(box, member, content);
            added.add(contentId);

            // 공유 박스에만 알림을 보낸다. MY 박스는 본인 활동이라 알릴 상대가 없다.
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
        return added;
    }

    private List<Long> removeContents(Member member, Box box, List<Long> removeIds, Map<Long, Content> contentById) {
        List<Long> removed = new ArrayList<>();
        for (Long contentId : removeIds) {
            Content content = contentById.get(contentId);
            if (content == null) {
                continue;
            }
            // 내가 담은 행만 찾는다. 남이 담은 것은 애초에 체크 상태로 보이지 않으므로 여기서 null 이다.
            BoxContent boxContent =
                    boxContentQueryService.getByBoxAndContentAndPublisherOrElseNull(box, content, member);
            if (boxContent == null) {
                continue;
            }
            boxContentCommandService.deleteContentFromBox(boxContent);
            boxHistoryCommandService.contentDeleted(box, member, content);
            removed.add(contentId);
        }
        return removed;
    }
}
