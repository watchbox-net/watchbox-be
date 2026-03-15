package net.watchbox.domain.content.api;

import lombok.RequiredArgsConstructor;
import net.watchbox.domain.content.base.dto.detail.ContentInfo;
import net.watchbox.domain.content.base.dto.detail.ContentDetailResponse;
import net.watchbox.domain.content.base.dto.interaction.MemberRecord;
import net.watchbox.domain.content.base.entity.MediaType;
import net.watchbox.domain.content.base.mapper.ContentDetailMapper;
import net.watchbox.domain.content.base.mapper.tmdb.TmdbContentDetailDtoMapper;
import net.watchbox.domain.content.base.service.ContentQueryService;
import net.watchbox.domain.record.entity.ContentRecord;
import net.watchbox.domain.record.service.ContentRecordService;
import net.watchbox.global.tmdb.service.TmdbMoviesService;
import net.watchbox.global.tmdb.service.TmdbPeopleService;
import net.watchbox.global.tmdb.service.TmdbTvSeriesService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ContentFacade {
    private final ContentQueryService contentQueryService;
    private final ContentRecordService contentRecordService;

    private final TmdbMoviesService tmdbMoviesService;
    private final TmdbTvSeriesService tmdbTvSeriesService;
    private final TmdbPeopleService tmdbPeopleService;

    @Transactional(readOnly = true)
    public ContentDetailResponse getContentDetailWithRecord(Long memberId, MediaType mediaType, Long contentId) {
        // 캐싱 후보
        ContentInfo contentInfo =
                switch (mediaType) {
                    case MOVIE -> ContentDetailMapper.fromMovie(contentQueryService.getMovieByIdOrThrow(contentId));
                    case TV -> ContentDetailMapper.fromTv(contentQueryService.getTvByIdOrThrow(contentId));
                    case PERSON -> ContentDetailMapper.fromPerson(contentQueryService.getPersonByIdOrThrow(contentId));
                };

        ContentRecord contentRecord = contentRecordService.getByMemberIdAndContentId(memberId, contentId);

        return ContentDetailResponse.builder()
                .mediaType(mediaType)
                .contentInfo(contentInfo)
                .memberRecord(MemberRecord.from(contentRecord))
                .build();
    }

    @Transactional(readOnly = true)
    public ContentDetailResponse getContentDetail(MediaType mediaType, Long contentId) {
        ContentInfo contentInfo =
                switch (mediaType) {
                    case MOVIE -> TmdbContentDetailDtoMapper.toMovieInfo(tmdbMoviesService.getMovieDetails(contentId));
                    case TV -> TmdbContentDetailDtoMapper.toTvInfo(tmdbTvSeriesService.getTvSeriesDetails(contentId));
                    case PERSON -> TmdbContentDetailDtoMapper.toPersonInfo(tmdbPeopleService.getPeopleDetails(contentId));
                };

        return ContentDetailResponse.builder()
                .mediaType(mediaType)
                .contentInfo(contentInfo)
                .build();
    }
}
