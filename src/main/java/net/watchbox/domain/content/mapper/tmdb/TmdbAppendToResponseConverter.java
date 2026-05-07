package net.watchbox.domain.content.mapper.tmdb;

import net.watchbox.domain.content.dto.detail.credit.person.Cast;
import net.watchbox.domain.content.dto.detail.credit.person.Crew;
import net.watchbox.domain.content.dto.detail.credit.person.PersonCredit;
import net.watchbox.domain.content.sub.person.entity.Department;
import net.watchbox.global.tmdb.inner.credit.TmdbCastItem;
import net.watchbox.global.tmdb.inner.credit.TmdbCrewItem;
import net.watchbox.global.tmdb.inner.image.TmdbImageItem;
import net.watchbox.global.tmdb.inner.watchprovider.TmdbCountryProviders;
import net.watchbox.global.tmdb.inner.watchprovider.TmdbWatchProviderItem;
import net.watchbox.global.tmdb.response.common.TmdbPersonCreditsResponse;
import net.watchbox.global.tmdb.response.common.TmdbWatchProvidersResponse;
import net.watchbox.global.tmdb.response.common.TmdbWorkImagesResponse;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
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
     * Movie credits → PersonCredit.
     * cast/crew 가 null 이면 빈 리스트로 처리.
     */
    public static PersonCredit toPersonCredit(TmdbPersonCreditsResponse credits) {
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
                .toList();
    }

    private static Crew toCrew(List<TmdbCrewItem> sameIdGroup) {
        TmdbCrewItem first = sameIdGroup.get(0);
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
}
