package net.watchbox.domain.content.common.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.watchbox.domain.content.common.entity.Content;
import net.watchbox.domain.content.common.entity.MediaType;
import net.watchbox.domain.content.common.repository.ContentRepository;
import net.watchbox.domain.content.movie.repository.MovieRepository;
import net.watchbox.domain.content.person.repository.PersonRepository;
import net.watchbox.domain.content.tv.repository.TvRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class ContentCommandService {
    private final MovieRepository movieRepository;
    private final TvRepository tvRepository;
    private final PersonRepository personRepository;
    private final ContentRepository contentRepository;

    private final ContentQueryService contentQueryService;

    // ToDo: (위변조 방지) 일단 만들어놓고 나중에 tmdb api 검색에서 없으면 위변조로 간주하고 DB에서 삭제할 것
    public Content createContent(Long tmdbId, MediaType mediaType) {
        contentRepository.insertContent(tmdbId, String.valueOf(mediaType));
        return contentQueryService.getContentByIdOrThrow(tmdbId);
    }

    public void createSubContent(Content content) {

    }
}
