package net.watchbox.admin;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.watchbox.admin.dto.TmdbContentItem;
import net.watchbox.admin.dto.TmdbWatchStatusItem;
import net.watchbox.domain.auth.entity.OauthAccount;
import net.watchbox.domain.auth.service.OauthAccountService;
import net.watchbox.domain.box.entity.box.Box;
import net.watchbox.domain.box.service.box.BoxService;
import net.watchbox.domain.box.service.content.BoxContentCommandService;
import net.watchbox.domain.content.entity.Content;
import net.watchbox.domain.content.service.ContentCommandService;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.member.service.MemberService;
import net.watchbox.domain.record.entity.ContentRecord;
import net.watchbox.domain.record.service.ContentRecordCommandService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AdminFacade {
    private final OauthAccountService oauthAccountService;
    private final MemberService memberService;
    private final BoxService boxService;
    private final ContentCommandService contentCommandService;
    private final BoxContentCommandService boxContentCommandService;
    private final ContentRecordCommandService contentRecordCommandService;

    @Transactional
    public void createSampleMember(Long id, String name, String email) {
        if(id >= -5){ // -1 ~ -5 는 테스트 계정
            throw new IllegalArgumentException("샘플 계정 ID는 음수여야 합니다.");
        }
        memberService.isNicknameAvailable(name);
        OauthAccount oauthAccount = oauthAccountService.createSampleAccount(id, name, email);
        memberService.createMember(oauthAccount);
    }

    @Transactional
    public void createBox(Long memberId, String boxName) {

    }

    @Transactional
    public void batchAddContentsToBox(Long memberId, Long boxId, List<TmdbContentItem> request) {
        Member member = memberService.getByMemberIdOrThrow(memberId);
        Box box = boxService.getByBoxIdOrElseThrow(boxId);
        for(TmdbContentItem item : request) {
            Content content = contentCommandService.getOrSaveContentCascade(item.tmdbId(), item.mediaType());
            boxContentCommandService.addContentToBox(member, box, content);
        }
    }

    @Transactional
    public void batchUpsertWatchStatus(Long memberId, List<TmdbWatchStatusItem> request) {
        Member member = memberService.getByMemberIdOrThrow(memberId);
        for(TmdbWatchStatusItem item : request) {
            Content content = contentCommandService.getOrSaveContentCascade(item.tmdbId(), item.watchMediaType().toMediaType());
            // ContentRecord 조회 or 생성
            ContentRecord contentRecord = contentRecordCommandService.getOrCreate(member, content);
            // WatchStatus 업데이트
            contentRecord.updateWatchStatus(item.watchStatus());
        }
    }
}
