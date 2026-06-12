package net.watchbox.domain.content.facade;

import lombok.RequiredArgsConstructor;
import net.watchbox.domain.box.service.content.BoxContentQueryService;
import net.watchbox.domain.content.dto.detail.ContentInfo;
import net.watchbox.domain.content.dto.detail.ContentDetailResponse;
import net.watchbox.domain.content.dto.interaction.MemberRecord;
import net.watchbox.domain.content.entity.Content;
import net.watchbox.domain.content.entity.MediaType;
import net.watchbox.domain.content.service.ContentQueryService;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.record.entity.record.ContentRecord;
import net.watchbox.domain.record.service.record.ContentRecordQueryService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ContentDetailFacade {
    private final ContentQueryService contentQueryService;
    private final ContentRecordQueryService contentRecordQueryService;
    private final BoxContentQueryService boxContentQueryService;

    @Transactional(readOnly = true)
    public ContentDetailResponse getContentDetail(MediaType mediaType, Long tmdbId, Member member) {
        ContentInfo contentInfo =
                switch (mediaType) {
                    case MOVIE -> contentQueryService.fromMovieDetails(tmdbId);
                    case TV -> contentQueryService.fromTvDetails(tmdbId);
                    case PERSON -> contentQueryService.fromPersonDetails(tmdbId);
                };

       if(member==null) { // member 요청 없으면 사용자 메타 데이터 없이 반환
           return ContentDetailResponse.builder()
                   .mediaType(mediaType)
                   .contentInfo(contentInfo)
                   .build();
       }

       Optional<Content> contentOptional = contentQueryService.findByTmdbIdAndMediaType(tmdbId, mediaType);

       if(contentOptional.isEmpty()) { // 저장된 Content 없으면 사용자 메타 데이터 없이 반환
           return ContentDetailResponse.builder()
                   .mediaType(mediaType)
                   .contentInfo(contentInfo)
                   .build();
       }

       Content content = contentOptional.get();
       ContentRecord contentRecord = contentRecordQueryService.getByMemberAndContent(member, content);
       boolean hasAddedInbox = boxContentQueryService.existsBoxContainingContentForMember(member, content);

       return ContentDetailResponse.builder()
               .mediaType(mediaType)
               .contentInfo(contentInfo)
               .memberRecord(MemberRecord.from(contentRecord))
               .hasAddedInbox(hasAddedInbox)
               .build();
    }

}
