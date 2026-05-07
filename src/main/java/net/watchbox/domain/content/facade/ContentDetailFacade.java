package net.watchbox.domain.content.facade;

import lombok.RequiredArgsConstructor;
import net.watchbox.domain.box.service.content.BoxContentQueryService;
import net.watchbox.domain.content.dto.detail.ContentInfo;
import net.watchbox.domain.content.dto.detail.ContentDetailResponse;
import net.watchbox.domain.content.dto.interaction.MemberRecord;
import net.watchbox.domain.content.entity.Content;
import net.watchbox.domain.content.entity.MediaType;
import net.watchbox.domain.content.mapper.tmdb.TmdbContentDetailDtoMapper;
import net.watchbox.domain.content.service.ContentQueryService;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.record.entity.ContentRecord;
import net.watchbox.domain.record.service.ContentRecordQueryService;
import net.watchbox.global.tmdb.service.TmdbMoviesService;
import net.watchbox.global.tmdb.service.TmdbPeopleService;
import net.watchbox.global.tmdb.service.TmdbTvSeriesService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ContentDetailFacade {
    private final ContentQueryService contentQueryService;
    private final ContentRecordQueryService contentRecordQueryService;

    private final TmdbMoviesService tmdbMoviesService;
    private final TmdbTvSeriesService tmdbTvSeriesService;
    private final TmdbPeopleService tmdbPeopleService;
    private final BoxContentQueryService boxContentQueryService;

    @Transactional(readOnly = true)
    public ContentDetailResponse getContentDetail(MediaType mediaType, Long tmdbId, Member member) {
        ContentInfo contentInfo =
                switch (mediaType) {
                    case MOVIE -> fromMovieDetails(tmdbId);
                    case TV -> fromTvDetails(tmdbId);
                    case PERSON -> fromPersonDetails(tmdbId);
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

    public ContentInfo fromMovieDetails(Long tmdbId) {
        // API 1) Detail & ko-KR & credits,watch/providers,videos -> 출연진/제작진, 플랫폼, (비디오)
        // API 2) Images & null
        return TmdbContentDetailDtoMapper.toMovieInfo(tmdbMoviesService.getMovieDetailsWithCVP(tmdbId));
    }

    public ContentInfo fromTvDetails(Long tmdbId) {
        // API 1) Detail & ko-KR & aggregate_credits,watch/providers,videos -> 역대 출연진/제작진, 플랫폼, (비디오)
        // API 2) Images & null
        return TmdbContentDetailDtoMapper.toTvInfo(tmdbTvSeriesService.getTvSeriesDetails(tmdbId));
    }

    public ContentInfo fromPersonDetails(Long tmdbId) {
        // API 1) Detail & ko-KR & combined_credits,images -> 기본 정보, 작품, (이미지)
        // API 2) Detail & null -> 영문 이름
        return TmdbContentDetailDtoMapper.toPersonInfo(tmdbPeopleService.getPeopleDetails(tmdbId));
    }

//    @Transactional(readOnly = true)
//    public ContentDetailResponse getContentDetailWithRecord(Long memberId, MediaType mediaType, Long contentId) {
//        // 캐싱 후보
//        ContentInfo contentInfo =
//                switch (mediaType) {
////                    case MOVIE -> ContentDetailMapper.fromMovie(contentQueryService.getMovieByIdOrThrow(contentId));
////                    case TV -> ContentDetailMapper.fromTv(contentQueryService.getTvByIdOrThrow(contentId));
////                    case PERSON -> ContentDetailMapper.fromPerson(contentQueryService.getPersonByIdOrThrow(contentId));
//                    case MOVIE -> TmdbContentDetailDtoMapper.toMovieInfo(tmdbMoviesService.getMovieDetails(contentId));
//                    case TV -> TmdbContentDetailDtoMapper.toTvInfo(tmdbTvSeriesService.getTvSeriesDetails(contentId));
//                    case PERSON -> TmdbContentDetailDtoMapper.toPersonInfo(tmdbPeopleService.getPeopleDetails(contentId));
//                };
//
//        ContentRecord contentRecord = contentRecordService.getByMemberIdAndContentIdOrElseNull(memberId, contentId);
//
//        return ContentDetailResponse.builder()
//                .mediaType(mediaType)
//                .contentInfo(contentInfo)
//                .memberRecord(MemberRecord.from(contentRecord))
//                .build();
//    }
}
