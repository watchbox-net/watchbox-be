package net.watchbox.global.tmdb.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * TMDB 가 응답 키로 사용하는 국가 코드 (ISO 3166-1 alpha-2).
 * 영문명/한글명 매핑 + Jackson 직렬화/역직렬화 지원.
 */
@Getter
@AllArgsConstructor
public enum Country {
    AE("United Arab Emirates", "아랍에미리트"),
    AR("Argentina", "아르헨티나"),
    AT("Austria", "오스트리아"),
    AU("Australia", "호주"),
    BA("Bosnia and Herzegovina", "보스니아 헤르체고비나"),
    BB("Barbados", "바베이도스"),
    BE("Belgium", "벨기에"),
    BG("Bulgaria", "불가리아"),
    BO("Bolivia", "볼리비아"),
    BR("Brazil", "브라질"),
    BS("Bahamas", "바하마"),
    CA("Canada", "캐나다"),
    CH("Switzerland", "스위스"),
    CI("Côte d'Ivoire", "코트디부아르"),
    CL("Chile", "칠레"),
    CO("Colombia", "콜롬비아"),
    CR("Costa Rica", "코스타리카"),
    CZ("Czechia", "체코"),
    DE("Germany", "독일"),
    DK("Denmark", "덴마크"),
    DO("Dominican Republic", "도미니카 공화국"),
    DZ("Algeria", "알제리"),
    EC("Ecuador", "에콰도르"),
    EG("Egypt", "이집트"),
    ES("Spain", "스페인"),
    FI("Finland", "핀란드"),
    FR("France", "프랑스"),
    GB("United Kingdom", "영국"),
    GF("French Guiana", "프랑스령 기아나"),
    GH("Ghana", "가나"),
    GQ("Equatorial Guinea", "적도 기니"),
    GT("Guatemala", "과테말라"),
    HK("Hong Kong", "홍콩"),
    HN("Honduras", "온두라스"),
    HR("Croatia", "크로아티아"),
    HU("Hungary", "헝가리"),
    ID("Indonesia", "인도네시아"),
    IE("Ireland", "아일랜드"),
    IL("Israel", "이스라엘"),
    IQ("Iraq", "이라크"),
    IT("Italy", "이탈리아"),
    JM("Jamaica", "자메이카"),
    JP("Japan", "일본"),
    KE("Kenya", "케냐"),
    KR("South Korea", "한국"),
    LB("Lebanon", "레바논"),
    LT("Lithuania", "리투아니아"),
    LY("Libya", "리비아"),
    MD("Moldova", "몰도바"),
    MK("North Macedonia", "북마케도니아"),
    MU("Mauritius", "모리셔스"),
    MX("Mexico", "멕시코"),
    MY("Malaysia", "말레이시아"),
    MZ("Mozambique", "모잠비크"),
    NE("Niger", "니제르"),
    NG("Nigeria", "나이지리아"),
    NL("Netherlands", "네덜란드"),
    NO("Norway", "노르웨이"),
    NZ("New Zealand", "뉴질랜드"),
    PA("Panama", "파나마"),
    PE("Peru", "페루"),
    PH("Philippines", "필리핀"),
    PL("Poland", "폴란드"),
    PS("Palestine", "팔레스타인"),
    PT("Portugal", "포르투갈"),
    PY("Paraguay", "파라과이"),
    RO("Romania", "루마니아"),
    RS("Serbia", "세르비아"),
    RU("Russia", "러시아"),
    SA("Saudi Arabia", "사우디아라비아"),
    SC("Seychelles", "세이셸"),
    SE("Sweden", "스웨덴"),
    SG("Singapore", "싱가포르"),
    SI("Slovenia", "슬로베니아"),
    SK("Slovakia", "슬로바키아"),
    SN("Senegal", "세네갈"),
    SV("El Salvador", "엘살바도르"),
    TH("Thailand", "태국"),
    TR("Türkiye", "튀르키예"),
    TT("Trinidad and Tobago", "트리니다드 토바고"),
    TW("Taiwan", "대만"),
    TZ("Tanzania", "탄자니아"),
    UG("Uganda", "우간다"),
    US("United States", "미국"),
    UY("Uruguay", "우루과이"),
    VE("Venezuela", "베네수엘라"),
    ZA("South Africa", "남아프리카 공화국"),
    ZM("Zambia", "잠비아");

    private final String englishValue;
    private final String koreanValue;

    private static final Map<String, Country> BY_CODE =
            Arrays.stream(values())
                    .collect(Collectors.toMap(Enum::name, c -> c));

    /**
     * 직렬화 시 ISO 코드(enum 이름)를 그대로 사용. 예: KR, US, JP
     */
    @JsonValue
    public String toJson() {
        return name();
    }

    /**
     * 역직렬화: ISO 코드로 enum 매칭. 알 수 없는 코드면 예외.
     */
    @JsonCreator
    public static Country fromCode(String code) {
        if (code == null) {
            return null;
        }
        Country c = BY_CODE.get(code);
        if (c == null) {
            throw new IllegalArgumentException("Unknown Country code: " + code);
        }
        return c;
    }

    public static String getKoreanByCode(String code) {
        Country c = BY_CODE.get(code);
        return c != null ? c.koreanValue : null;
    }

    public static String getEnglishByCode(String code) {
        Country c = BY_CODE.get(code);
        return c != null ? c.englishValue : null;
    }
}
