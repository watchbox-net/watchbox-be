package net.watchpeople.domain.content.common.service;

import lombok.RequiredArgsConstructor;
import net.watchpeople.domain.content.common.repository.ContentRepository;
import net.watchpeople.domain.content.movie.repository.MovieRepository;
import net.watchpeople.domain.content.person.repository.PersonRepository;
import net.watchpeople.domain.content.tv.repository.TvRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContentQueryService {
    private final MovieRepository movieRepository;
    private final TvRepository tvRepository;
    private final PersonRepository personRepository;
    private final ContentRepository contentRepository;
}
