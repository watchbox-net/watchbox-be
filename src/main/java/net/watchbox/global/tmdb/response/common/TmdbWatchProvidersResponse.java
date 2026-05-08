package net.watchbox.global.tmdb.response.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.watchbox.global.tmdb.inner.watchprovider.TmdbCountryProviders;

import java.util.Map;

/**
 * TMDB watch/providers 응답.
 * results 의 키는 ISO 3166-1 alpha-2 국가 코드 (예: "KR", "US", "JP").
 * Country enum 사용 시 미정의 국가가 들어오면 예외 발생 가능하므로 String 으로 받음.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TmdbWatchProvidersResponse {
    private Map<String, TmdbCountryProviders> results;
}
