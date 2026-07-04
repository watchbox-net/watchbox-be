package net.watchbox.domain.content.mapper.tmdb;

import net.watchbox.domain.content.dto.detail.credit.person.AggregatePersonCredit;
import net.watchbox.domain.content.dto.detail.credit.person.Cast;
import net.watchbox.domain.content.dto.detail.credit.person.Crew;
import net.watchbox.domain.content.dto.detail.credit.person.PersonCredit;
import net.watchbox.domain.content.dto.detail.credit.work.CombinedCredit;
import net.watchbox.domain.content.dto.detail.credit.work.CreditRole;
import net.watchbox.domain.content.dto.detail.credit.work.MovieCredit;
import net.watchbox.domain.content.dto.detail.credit.work.TvCredit;
import net.watchbox.domain.content.dto.detail.credit.work.WorkCredit;
import net.watchbox.global.tmdb.configuration.Department;
import net.watchbox.domain.record.dto.type.WatchMediaType;
import net.watchbox.global.tmdb.inner.credit.movie.TmdbCastItem;
import net.watchbox.global.tmdb.inner.credit.movie.TmdbCrewItem;
import net.watchbox.global.tmdb.inner.credit.tv.TmdbAggregateCastItem;
import net.watchbox.global.tmdb.inner.credit.tv.TmdbAggregateCastRoleItem;
import net.watchbox.global.tmdb.inner.credit.tv.TmdbAggregateCrewItem;
import net.watchbox.global.tmdb.inner.image.TmdbImageItem;
import net.watchbox.global.tmdb.inner.watchprovider.TmdbCountryProviders;
import net.watchbox.global.tmdb.inner.watchprovider.TmdbWatchProviderItem;
import net.watchbox.global.tmdb.response.common.TmdbAggregateCreditsResponse;
import net.watchbox.global.tmdb.response.common.TmdbCombinedCreditsResponse;
import net.watchbox.global.tmdb.response.common.TmdbCreditsResponse;
import net.watchbox.global.tmdb.response.common.TmdbPersonImagesResponse;
import net.watchbox.global.tmdb.response.common.TmdbWatchProvidersResponse;
import net.watchbox.global.tmdb.response.common.TmdbWorkImagesResponse;
import net.watchbox.global.tmdb.util.TmdbUtils;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * append_to_response 로 받은 TMDB 응답 조각들을 도메인 DTO 로 변환.
 */
public class TmdbAppendToResponseConverter {

    private TmdbAppendToResponseConverter() {
    }

    /**
     * Movie credits → Credit.
     * cast/crew 가 null 이면 빈 리스트로 처리.
     */
    public static PersonCredit toPersonCredit(TmdbCreditsResponse credits) {
        if (credits == null) {
            return PersonCredit.builder()
                    .castList(List.of())
                    .crewList(List.of())
                    .totalCount(0L)
                    .castCount(0L)
                    .crewCount(0L)
                    .build();
        }

        List<Cast> castList = credits.getCast() == null ? List.of()
                : credits.getCast().stream().map(TmdbAppendToResponseConverter::toCast).toList();
        List<Crew> crewList = credits.getCrew() == null ? List.of()
                : groupCrewByPerson(credits.getCrew());

        long castCount = castList.size();
        long crewCount = crewList.size();

        return PersonCredit.builder()
                .castList(castList)
                .crewList(crewList)
                .totalCount(castCount + crewCount)
                .castCount(castCount)
                .crewCount(crewCount)
                .build();
    }

    private static Cast toCast(TmdbCastItem item) {
        return Cast.builder()
                .tmdbId(item.getId())
                .profilePath(item.getProfilePath())
                .name(item.getName())
                .nameOriginal(item.getOriginalName())
                .knownForDepartment(Department.fromEnglishValue(item.getKnownForDepartment()))
                .character(item.getCharacter())
                .order(item.getOrder() != null ? item.getOrder().longValue() : null)
                .build();
    }

    /**
     * crew 응답을 인물 단위로 그룹핑.
     * 같은 인물이 여러 부서/직무로 등장하는 경우(감독+제작 등)를 한 row 로 합쳐 jobList 로 표현.
     * 등장 순서(LinkedHashMap)는 TMDB 가 보내준 순서를 그대로 유지.
     * 단, 감독(DIRECTING) 부서를 가진 인물은 stable sort 로 맨 앞에 배치.
     */
    private static List<Crew> groupCrewByPerson(List<TmdbCrewItem> rawCrewList) {
        return rawCrewList.stream()
                .collect(Collectors.groupingBy(
                        TmdbCrewItem::getId,
                        LinkedHashMap::new,
                        Collectors.toList()
                ))
                .values().stream()
                .map(TmdbAppendToResponseConverter::toCrew)
                .sorted(Comparator.comparingInt(crew ->
                        crew.getDepartmentList().contains(Department.DIRECTING) ? 0 : 1))
                .toList();
    }

    private static Crew toCrew(List<TmdbCrewItem> sameIdGroup) {
        TmdbCrewItem first = sameIdGroup.getFirst();
        // 한 인물이 작품에서 여러 부서를 겸한 경우(예: 감독+각본) 모두 보존
        List<Department> departmentList = sameIdGroup.stream()
                .map(TmdbCrewItem::getDepartment)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        return Crew.builder()
                .tmdbId(first.getId())
                .profilePath(first.getProfilePath())
                .name(first.getName())
                .nameOriginal(first.getOriginalName())
                .knownForDepartment(Department.fromEnglishValue(first.getKnownForDepartment()))
                .departmentList(departmentList)
                .build();
    }

    /**
     * TV 시리즈 aggregate_credits → AggregatePersonCredit.
     * cast/crew 가 null 이면 빈 리스트로 처리.
     * 영화 credits 와 달리 시즌별 누적 데이터라 한 인물이 여러 캐릭터/직무를 가질 수 있음.
     */
    public static AggregatePersonCredit toAggregatePersonCredit(TmdbAggregateCreditsResponse credits) {
        if (credits == null) {
            return AggregatePersonCredit.builder()
                    .castList(List.of())
                    .crewList(List.of())
                    .totalCount(0L)
                    .castCount(0L)
                    .crewCount(0L)
                    .build();
        }

        List<Cast> castList = credits.getCast() == null ? List.of()
                : credits.getCast().stream().map(TmdbAppendToResponseConverter::toCastFromAggregate).toList();
        List<Crew> crewList = credits.getCrew() == null ? List.of()
                : groupAggregateCrewByPerson(credits.getCrew());

        long castCount = castList.size();
        long crewCount = crewList.size();

        return AggregatePersonCredit.builder()
                .castList(castList)
                .crewList(crewList)
                .totalCount(castCount + crewCount)
                .castCount(castCount)
                .crewCount(crewCount)
                .build();
    }

    private static Cast toCastFromAggregate(TmdbAggregateCastItem item) {
        // 한 인물이 시리즈 내 여러 역할을 맡은 경우(시즌별 다른 캐릭터, 어린 시절 역 등) 구분자로 합침
        String character = item.getRoles() == null ? null
                : item.getRoles().stream()
                        .map(TmdbAggregateCastRoleItem::getCharacter)
                        .filter(Objects::nonNull)
                        .distinct()
                        .collect(Collectors.joining(", "));
        return Cast.builder()
                .tmdbId(item.getId())
                .profilePath(item.getProfilePath())
                .name(item.getName())
                .nameOriginal(item.getOriginalName())
                .knownForDepartment(Department.fromEnglishValue(item.getKnownForDepartment()))
                .character(character)
                .order(item.getOrder() != null ? item.getOrder().longValue() : null)
                .build();
    }

    /**
     * aggregate crew 응답을 인물 단위로 그룹핑.
     * TMDB 가 같은 인물이라도 부서가 다르면 별도 row 로 보내므로 (예: 감독+제작) 인물 단위로 합침.
     * 감독(DIRECTING) 부서를 가진 인물은 stable sort 로 맨 앞에 배치.
     */
    private static List<Crew> groupAggregateCrewByPerson(List<TmdbAggregateCrewItem> rawCrewList) {
        return rawCrewList.stream()
                .collect(Collectors.groupingBy(
                        TmdbAggregateCrewItem::getId,
                        LinkedHashMap::new,
                        Collectors.toList()
                ))
                .values().stream()
                .map(TmdbAppendToResponseConverter::toCrewFromAggregate)
                .sorted(Comparator.comparingInt(crew ->
                        crew.getDepartmentList().contains(Department.DIRECTING) ? 0 : 1))
                .toList();
    }

    private static Crew toCrewFromAggregate(List<TmdbAggregateCrewItem> sameIdGroup) {
        TmdbAggregateCrewItem first = sameIdGroup.getFirst();
        List<Department> departmentList = sameIdGroup.stream()
                .map(TmdbAggregateCrewItem::getDepartment)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        return Crew.builder()
                .tmdbId(first.getId())
                .profilePath(first.getProfilePath())
                .name(first.getName())
                .nameOriginal(first.getOriginalName())
                .knownForDepartment(Department.fromEnglishValue(first.getKnownForDepartment()))
                .departmentList(departmentList)
                .build();
    }

    /**
     * Person combined_credits → WorkCredit.
     * cast/crew 모두 합치고 (tmdbId, mediaType) 기준으로 그룹핑 — 같은 작품에 출연+제작 동시 등장하면 1 row.
     * 최신 날짜순(내림차순) 정렬, 날짜 없는 항목(미공개작 등)은 맨 뒤로 밀림.
     */
    public static WorkCredit toWorkCredit(TmdbCombinedCreditsResponse credits) {
        if (credits == null) {
            return WorkCredit.builder()
                    .combinedCreditList(List.of())
                    .totalCount(0L)
                    .build();
        }

        // 1. cast/crew 모두 동일한 raw 구조로 통일
        Stream<RawCredit> castRaw = credits.getCast() == null ? Stream.empty()
                : credits.getCast().stream().map(item -> {
                    boolean isMovie = item.isMovie();
                    String dateStr = isMovie ? item.getReleaseDate() : item.getFirstAirDate();
                    return new RawCredit(
                            item.getId(),
                            isMovie ? WatchMediaType.MOVIE : WatchMediaType.TV,
                            item.getPosterPath(),
                            isMovie ? item.getTitle() : item.getName(),
                            CreditRole.CAST, item.getCharacter(), null,
                            TmdbUtils.extractYear(dateStr),
                            TmdbUtils.extractDate(dateStr),
                            item.getPopularity()
                    );
                });

        Stream<RawCredit> crewRaw = credits.getCrew() == null ? Stream.empty()
                : credits.getCrew().stream().map(item -> {
                    boolean isMovie = item.isMovie();
                    String dateStr = isMovie ? item.getReleaseDate() : item.getFirstAirDate();
                    return new RawCredit(
                            item.getId(),
                            isMovie ? WatchMediaType.MOVIE : WatchMediaType.TV,
                            item.getPosterPath(),
                            isMovie ? item.getTitle() : item.getName(),
                            CreditRole.CREW, null, item.getDepartment(),
                            TmdbUtils.extractYear(dateStr),
                            TmdbUtils.extractDate(dateStr),
                            item.getPopularity()
                    );
                });

        // 2. (tmdbId + mediaType) 기준 그룹핑 — 같은 작품 합치기
        Map<WorkKey, List<RawCredit>> grouped = Stream.concat(castRaw, crewRaw)
                .collect(Collectors.groupingBy(
                        r -> new WorkKey(r.tmdbId(), r.mediaType()),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        // 3. 그룹별로 합쳐서 CombinedCredit 빌드 + 최신 날짜순 정렬
        List<CombinedCredit> combinedCreditList = grouped.values().stream()
                .map(TmdbAppendToResponseConverter::mergeWorkGroup)
                .sorted(Comparator.comparing(
                        CombinedCredit::getSortDate,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .toList();

        return WorkCredit.builder()
                .combinedCreditList(combinedCreditList)
                .totalCount((long) combinedCreditList.size())
                .build();
    }

    /** 같은 (tmdbId, mediaType) 작품 그룹을 하나의 CombinedCredit 로 머지 */
    private static CombinedCredit mergeWorkGroup(List<RawCredit> group) {
        RawCredit first = group.getFirst();
        List<CreditRole> roles = group.stream()
                .map(RawCredit::role).distinct().toList();
        String character = group.stream()
                .map(RawCredit::character)
                .filter(Objects::nonNull)
                .findFirst().orElse(null);
        List<Department> departments = group.stream()
                .map(RawCredit::department)
                .filter(Objects::nonNull)
                .distinct().toList();

        if (first.mediaType() == WatchMediaType.MOVIE) {
            return MovieCredit.builder()
                    .tmdbId(first.tmdbId())
                    .watchMediaType(WatchMediaType.MOVIE)
                    .posterPath(first.posterPath())
                    .title(first.workName())
                    .creditRoleList(roles)
                    .character(character)
                    .departmentList(departments)
                    .year(first.year())
                    .releaseDate(first.sortDate())
                    .popularity(first.popularity())
                    .build();
        }
        return TvCredit.builder()
                .tmdbId(first.tmdbId())
                .watchMediaType(WatchMediaType.TV)
                .posterPath(first.posterPath())
                .name(first.workName())
                .creditRoleList(roles)
                .character(character)
                .departmentList(departments)
                .year(first.year())
                .firstAirDate(first.sortDate())
                .popularity(first.popularity())
                .build();
    }

    /** 그룹핑용 내부 키 — tmdbId 만으로는 movie/tv 충돌 가능하므로 mediaType 도 포함 */
    private record WorkKey(Long tmdbId, WatchMediaType mediaType) {}

    /** cast/crew 항목을 동일 구조로 정규화한 중간 표현 */
    private record RawCredit(
            Long tmdbId,
            WatchMediaType mediaType,
            String posterPath,
            String workName,       // movie.title 또는 tv.name
            CreditRole role,
            String character,      // cast 일 때만
            Department department, // crew 일 때만
            Integer year,
            LocalDate sortDate,
            Double popularity
    ) {}

    /**
     * WatchProviders → 한국(KR) 기준 provider 이름 리스트.
     * flatrate / buy / rent 모두 합쳐서 distinct 처리.
     */
    public static List<String> toWatchProviderList(TmdbWatchProvidersResponse watchProviders) {
        if (watchProviders == null || watchProviders.getResults() == null) {
            return Collections.emptyList();
        }
        TmdbCountryProviders kr = watchProviders.getResults().get("KR");
        if (kr == null) {
            return Collections.emptyList();
        }
        return Stream.of(kr.getFlatrate(), kr.getBuy(), kr.getRent())
                .filter(list -> list != null)
                .flatMap(List::stream)
                .map(TmdbWatchProviderItem::getProviderName)
                .filter(name -> name != null)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Images.backdrops → file_path 리스트.
     * TMDB 가 voteAverage 내림차순으로 정렬해서 보내주므로 순서 그대로 사용.
     */
    public static List<String> toBackdropPathList(TmdbWorkImagesResponse imagesResponse) {
        if (imagesResponse == null || imagesResponse.getBackdrops() == null) {
            return Collections.emptyList();
        }
        return imagesResponse.getBackdrops().stream()
                .map(TmdbImageItem::getFilePath)
                .filter(path -> path != null)
                .toList();
    }

    /**
     * Person Images.profiles → file_path 리스트.
     * TMDB 가 voteAverage 내림차순으로 정렬해서 보내주므로 순서 그대로 사용.
     */
    public static List<String> toProfilePathList(TmdbPersonImagesResponse imagesResponse) {
        if (imagesResponse == null || imagesResponse.getProfiles() == null) {
            return Collections.emptyList();
        }
        return imagesResponse.getProfiles().stream()
                .map(TmdbImageItem::getFilePath)
                .filter(path -> path != null)
                .toList();
    }
}
