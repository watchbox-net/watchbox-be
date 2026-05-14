package net.watchbox.application.admin;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.watchbox.application.admin.dto.TmdbContentItem;
import net.watchbox.application.admin.dto.TmdbWatchStatusItem;
import net.watchbox.domain.auth.entity.OauthAccount;
import net.watchbox.domain.auth.service.OauthAccountService;
import net.watchbox.domain.box.dto.box.BoxCreateRequest;
import net.watchbox.domain.box.dto.box.BoxCreateResponse;
import net.watchbox.domain.box.entity.box.Box;
import net.watchbox.domain.box.service.box.BoxService;
import net.watchbox.domain.box.service.content.BoxContentCommandService;
import net.watchbox.domain.content.entity.Content;
import net.watchbox.domain.content.service.ContentCommandService;
import net.watchbox.domain.member.dto.response.ProfileResponse;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.member.service.MemberService;
import net.watchbox.domain.record.entity.ContentRecord;
import net.watchbox.domain.record.service.ContentRecordCommandService;
import net.watchbox.global.dto.response.exception.CustomException;
import net.watchbox.global.dto.response.exception.ErrorCode;
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
    public ProfileResponse createSampleMember(String name, String nickname, String email) {
        if(oauthAccountService.existsByName(name)) {
            throw new CustomException(ErrorCode.NAME_ALREADY_EXISTS);
        }
        if(memberService.existsByNickname(nickname)){
            throw new CustomException(ErrorCode.NICKNAME_ALREADY_EXISTS);
        }
        if(memberService.existsByEmail(email)){
            throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        OauthAccount oauthAccount = oauthAccountService.createSampleAccount(name, email);
        Member member = memberService.createSampleMember(oauthAccount, nickname);
        boxService.createInitialMyBox(member);
        return ProfileResponse.from(member);
    }

    @Transactional
    public BoxCreateResponse createBox(String nickname, BoxCreateRequest request) {
        Member member = memberService.getByNicknameOrThrow(nickname);
        Box box = boxService.createBox(member, request);
        return BoxCreateResponse.from(box);
    }

    @Transactional
    public void batchAddContentsToBox(String nickname, Long boxId, List<TmdbContentItem> request) {
        Member member = memberService.getByNicknameOrThrow(nickname);
        Box box = boxService.getByBoxIdOrElseThrow(boxId);
        for(TmdbContentItem item : request) {
            Content content = contentCommandService.getOrSaveContentCascade(item.tmdbId(), item.mediaType());
            boxContentCommandService.addContentToBox(member, box, content);
        }
    }

    @Transactional
    public void batchUpsertWatchStatus(String nickname, List<TmdbWatchStatusItem> request) {
        Member member = memberService.getByNicknameOrThrow(nickname);
        for(TmdbWatchStatusItem item : request) {
            Content content = contentCommandService.getOrSaveContentCascade(item.tmdbId(), item.watchMediaType().toMediaType());
            // ContentRecord 조회 or 생성
            ContentRecord contentRecord = contentRecordCommandService.getOrCreate(member, content);
            // WatchStatus 업데이트
            contentRecord.updateWatchStatus(item.watchStatus());
        }
    }
}
