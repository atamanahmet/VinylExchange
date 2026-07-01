package com.atamanahmet.vinylexchange.domain.enums;

import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * ISO 3166-1 alpha-2 countries.
 */
public enum Country {
    ANDORRA("AD", CountryTier.STANDARD),
    UNITED_ARAB_EMIRATES("AE", CountryTier.STANDARD),
    AFGHANISTAN("AF", CountryTier.STANDARD),
    ANTIGUA_BARBUDA("AG", CountryTier.STANDARD),
    ANGUILLA("AI", CountryTier.STANDARD),
    ALBANIA("AL", CountryTier.STANDARD),
    ARMENIA("AM", CountryTier.STANDARD),
    ANGOLA("AO", CountryTier.STANDARD),
    ANTARCTICA("AQ", CountryTier.STANDARD),
    ARGENTINA("AR", CountryTier.STANDARD),
    AMERICAN_SAMOA("AS", CountryTier.STANDARD),
    AUSTRIA("AT", CountryTier.STANDARD),
    AUSTRALIA("AU", CountryTier.STANDARD),
    ARUBA("AW", CountryTier.STANDARD),
    ALAND_ISLANDS("AX", CountryTier.STANDARD),
    AZERBAIJAN("AZ", CountryTier.STANDARD),
    BOSNIA_HERZEGOVINA("BA", CountryTier.STANDARD),
    BARBADOS("BB", CountryTier.STANDARD),
    BANGLADESH("BD", CountryTier.STANDARD),
    BELGIUM("BE", CountryTier.STANDARD),
    BURKINA_FASO("BF", CountryTier.STANDARD),
    BULGARIA("BG", CountryTier.STANDARD),
    BAHRAIN("BH", CountryTier.STANDARD),
    BURUNDI("BI", CountryTier.STANDARD),
    BENIN("BJ", CountryTier.STANDARD),
    ST_BARTHELEMY("BL", CountryTier.STANDARD),
    BERMUDA("BM", CountryTier.STANDARD),
    BRUNEI("BN", CountryTier.STANDARD),
    BOLIVIA("BO", CountryTier.STANDARD),
    CARIBBEAN_NETHERLANDS("BQ", CountryTier.STANDARD),
    BRAZIL("BR", CountryTier.STANDARD),
    BAHAMAS("BS", CountryTier.STANDARD),
    BHUTAN("BT", CountryTier.STANDARD),
    BOUVET_ISLAND("BV", CountryTier.STANDARD),
    BOTSWANA("BW", CountryTier.STANDARD),
    BELARUS("BY", CountryTier.STANDARD),
    BELIZE("BZ", CountryTier.STANDARD),
    CANADA("CA", CountryTier.STANDARD),
    COCOS_KEELING_ISLANDS("CC", CountryTier.STANDARD),
    CONGO_KINSHASA("CD", CountryTier.STANDARD),
    CENTRAL_AFRICAN_REPUBLIC("CF", CountryTier.STANDARD),
    CONGO_BRAZZAVILLE("CG", CountryTier.STANDARD),
    SWITZERLAND("CH", CountryTier.STANDARD),
    COTE_D_IVOIRE("CI", CountryTier.STANDARD),
    COOK_ISLANDS("CK", CountryTier.STANDARD),
    CHILE("CL", CountryTier.STANDARD),
    CAMEROON("CM", CountryTier.STANDARD),
    CHINA("CN", CountryTier.STANDARD),
    COLOMBIA("CO", CountryTier.STANDARD),
    COSTA_RICA("CR", CountryTier.STANDARD),
    CUBA("CU", CountryTier.STANDARD),
    CAPE_VERDE("CV", CountryTier.STANDARD),
    CURACAO("CW", CountryTier.STANDARD),
    CHRISTMAS_ISLAND("CX", CountryTier.STANDARD),
    CYPRUS("CY", CountryTier.STANDARD),
    CZECHIA("CZ", CountryTier.STANDARD),
    GERMANY("DE", CountryTier.COMMON),
    DJIBOUTI("DJ", CountryTier.STANDARD),
    DENMARK("DK", CountryTier.STANDARD),
    DOMINICA("DM", CountryTier.STANDARD),
    DOMINICAN_REPUBLIC("DO", CountryTier.STANDARD),
    ALGERIA("DZ", CountryTier.STANDARD),
    ECUADOR("EC", CountryTier.STANDARD),
    ESTONIA("EE", CountryTier.STANDARD),
    EGYPT("EG", CountryTier.STANDARD),
    WESTERN_SAHARA("EH", CountryTier.STANDARD),
    ERITREA("ER", CountryTier.STANDARD),
    SPAIN("ES", CountryTier.STANDARD),
    ETHIOPIA("ET", CountryTier.STANDARD),
    FINLAND("FI", CountryTier.STANDARD),
    FIJI("FJ", CountryTier.STANDARD),
    FALKLAND_ISLANDS("FK", CountryTier.STANDARD),
    MICRONESIA("FM", CountryTier.STANDARD),
    FAROE_ISLANDS("FO", CountryTier.STANDARD),
    FRANCE("FR", CountryTier.COMMON),
    GABON("GA", CountryTier.STANDARD),
    UNITED_KINGDOM("GB", CountryTier.COMMON),
    GRENADA("GD", CountryTier.STANDARD),
    GEORGIA("GE", CountryTier.STANDARD),
    FRENCH_GUIANA("GF", CountryTier.STANDARD),
    GUERNSEY("GG", CountryTier.STANDARD),
    GHANA("GH", CountryTier.STANDARD),
    GIBRALTAR("GI", CountryTier.STANDARD),
    GREENLAND("GL", CountryTier.STANDARD),
    GAMBIA("GM", CountryTier.STANDARD),
    GUINEA("GN", CountryTier.STANDARD),
    GUADELOUPE("GP", CountryTier.STANDARD),
    EQUATORIAL_GUINEA("GQ", CountryTier.STANDARD),
    GREECE("GR", CountryTier.STANDARD),
    SOUTH_GEORGIA_SOUTH_SANDWICH_ISLANDS("GS", CountryTier.STANDARD),
    GUATEMALA("GT", CountryTier.STANDARD),
    GUAM("GU", CountryTier.STANDARD),
    GUINEA_BISSAU("GW", CountryTier.STANDARD),
    GUYANA("GY", CountryTier.STANDARD),
    HONG_KONG_SAR_CHINA("HK", CountryTier.STANDARD),
    HEARD_MCDONALD_ISLANDS("HM", CountryTier.STANDARD),
    HONDURAS("HN", CountryTier.STANDARD),
    CROATIA("HR", CountryTier.STANDARD),
    HAITI("HT", CountryTier.STANDARD),
    HUNGARY("HU", CountryTier.STANDARD),
    INDONESIA("ID", CountryTier.STANDARD),
    IRELAND("IE", CountryTier.STANDARD),
    ISRAEL("IL", CountryTier.STANDARD),
    ISLE_OF_MAN("IM", CountryTier.STANDARD),
    INDIA("IN", CountryTier.STANDARD),
    BRITISH_INDIAN_OCEAN_TERRITORY("IO", CountryTier.STANDARD),
    IRAQ("IQ", CountryTier.STANDARD),
    IRAN("IR", CountryTier.STANDARD),
    ICELAND("IS", CountryTier.STANDARD),
    ITALY("IT", CountryTier.STANDARD),
    JERSEY("JE", CountryTier.STANDARD),
    JAMAICA("JM", CountryTier.STANDARD),
    JORDAN("JO", CountryTier.STANDARD),
    JAPAN("JP", CountryTier.COMMON),
    KENYA("KE", CountryTier.STANDARD),
    KYRGYZSTAN("KG", CountryTier.STANDARD),
    CAMBODIA("KH", CountryTier.STANDARD),
    KIRIBATI("KI", CountryTier.STANDARD),
    COMOROS("KM", CountryTier.STANDARD),
    ST_KITTS_NEVIS("KN", CountryTier.STANDARD),
    NORTH_KOREA("KP", CountryTier.STANDARD),
    SOUTH_KOREA("KR", CountryTier.STANDARD),
    KUWAIT("KW", CountryTier.STANDARD),
    CAYMAN_ISLANDS("KY", CountryTier.STANDARD),
    KAZAKHSTAN("KZ", CountryTier.STANDARD),
    LAOS("LA", CountryTier.STANDARD),
    LEBANON("LB", CountryTier.STANDARD),
    ST_LUCIA("LC", CountryTier.STANDARD),
    LIECHTENSTEIN("LI", CountryTier.STANDARD),
    SRI_LANKA("LK", CountryTier.STANDARD),
    LIBERIA("LR", CountryTier.STANDARD),
    LESOTHO("LS", CountryTier.STANDARD),
    LITHUANIA("LT", CountryTier.STANDARD),
    LUXEMBOURG("LU", CountryTier.STANDARD),
    LATVIA("LV", CountryTier.STANDARD),
    LIBYA("LY", CountryTier.STANDARD),
    MOROCCO("MA", CountryTier.STANDARD),
    MONACO("MC", CountryTier.STANDARD),
    MOLDOVA("MD", CountryTier.STANDARD),
    MONTENEGRO("ME", CountryTier.STANDARD),
    ST_MARTIN("MF", CountryTier.STANDARD),
    MADAGASCAR("MG", CountryTier.STANDARD),
    MARSHALL_ISLANDS("MH", CountryTier.STANDARD),
    NORTH_MACEDONIA("MK", CountryTier.STANDARD),
    MALI("ML", CountryTier.STANDARD),
    MYANMAR_BURMA("MM", CountryTier.STANDARD),
    MONGOLIA("MN", CountryTier.STANDARD),
    MACAO_SAR_CHINA("MO", CountryTier.STANDARD),
    NORTHERN_MARIANA_ISLANDS("MP", CountryTier.STANDARD),
    MARTINIQUE("MQ", CountryTier.STANDARD),
    MAURITANIA("MR", CountryTier.STANDARD),
    MONTSERRAT("MS", CountryTier.STANDARD),
    MALTA("MT", CountryTier.STANDARD),
    MAURITIUS("MU", CountryTier.STANDARD),
    MALDIVES("MV", CountryTier.STANDARD),
    MALAWI("MW", CountryTier.STANDARD),
    MEXICO("MX", CountryTier.STANDARD),
    MALAYSIA("MY", CountryTier.STANDARD),
    MOZAMBIQUE("MZ", CountryTier.STANDARD),
    NAMIBIA("NA", CountryTier.STANDARD),
    NEW_CALEDONIA("NC", CountryTier.STANDARD),
    NIGER("NE", CountryTier.STANDARD),
    NORFOLK_ISLAND("NF", CountryTier.STANDARD),
    NIGERIA("NG", CountryTier.STANDARD),
    NICARAGUA("NI", CountryTier.STANDARD),
    NETHERLANDS("NL", CountryTier.STANDARD),
    NORWAY("NO", CountryTier.STANDARD),
    NEPAL("NP", CountryTier.STANDARD),
    NAURU("NR", CountryTier.STANDARD),
    NIUE("NU", CountryTier.STANDARD),
    NEW_ZEALAND("NZ", CountryTier.STANDARD),
    OMAN("OM", CountryTier.STANDARD),
    PANAMA("PA", CountryTier.STANDARD),
    PERU("PE", CountryTier.STANDARD),
    FRENCH_POLYNESIA("PF", CountryTier.STANDARD),
    PAPUA_NEW_GUINEA("PG", CountryTier.STANDARD),
    PHILIPPINES("PH", CountryTier.STANDARD),
    PAKISTAN("PK", CountryTier.STANDARD),
    POLAND("PL", CountryTier.STANDARD),
    ST_PIERRE_MIQUELON("PM", CountryTier.STANDARD),
    PITCAIRN_ISLANDS("PN", CountryTier.STANDARD),
    PUERTO_RICO("PR", CountryTier.STANDARD),
    PALESTINIAN_TERRITORIES("PS", CountryTier.STANDARD),
    PORTUGAL("PT", CountryTier.STANDARD),
    PALAU("PW", CountryTier.STANDARD),
    PARAGUAY("PY", CountryTier.STANDARD),
    QATAR("QA", CountryTier.STANDARD),
    REUNION("RE", CountryTier.STANDARD),
    ROMANIA("RO", CountryTier.STANDARD),
    SERBIA("RS", CountryTier.STANDARD),
    RUSSIA("RU", CountryTier.STANDARD),
    RWANDA("RW", CountryTier.STANDARD),
    SAUDI_ARABIA("SA", CountryTier.STANDARD),
    SOLOMON_ISLANDS("SB", CountryTier.STANDARD),
    SEYCHELLES("SC", CountryTier.STANDARD),
    SUDAN("SD", CountryTier.STANDARD),
    SWEDEN("SE", CountryTier.STANDARD),
    SINGAPORE("SG", CountryTier.STANDARD),
    ST_HELENA("SH", CountryTier.STANDARD),
    SLOVENIA("SI", CountryTier.STANDARD),
    SVALBARD_JAN_MAYEN("SJ", CountryTier.STANDARD),
    SLOVAKIA("SK", CountryTier.STANDARD),
    SIERRA_LEONE("SL", CountryTier.STANDARD),
    SAN_MARINO("SM", CountryTier.STANDARD),
    SENEGAL("SN", CountryTier.STANDARD),
    SOMALIA("SO", CountryTier.STANDARD),
    SURINAME("SR", CountryTier.STANDARD),
    SOUTH_SUDAN("SS", CountryTier.STANDARD),
    SAO_TOME_PRINCIPE("ST", CountryTier.STANDARD),
    EL_SALVADOR("SV", CountryTier.STANDARD),
    SINT_MAARTEN("SX", CountryTier.STANDARD),
    SYRIA("SY", CountryTier.STANDARD),
    ESWATINI("SZ", CountryTier.STANDARD),
    TURKS_CAICOS_ISLANDS("TC", CountryTier.STANDARD),
    CHAD("TD", CountryTier.STANDARD),
    FRENCH_SOUTHERN_TERRITORIES("TF", CountryTier.STANDARD),
    TOGO("TG", CountryTier.STANDARD),
    THAILAND("TH", CountryTier.STANDARD),
    TAJIKISTAN("TJ", CountryTier.STANDARD),
    TOKELAU("TK", CountryTier.STANDARD),
    TIMOR_LESTE("TL", CountryTier.STANDARD),
    TURKMENISTAN("TM", CountryTier.STANDARD),
    TUNISIA("TN", CountryTier.STANDARD),
    TONGA("TO", CountryTier.STANDARD),
    TURKEY("TR", CountryTier.PRIMARY),
    TRINIDAD_TOBAGO("TT", CountryTier.STANDARD),
    TUVALU("TV", CountryTier.STANDARD),
    TAIWAN("TW", CountryTier.STANDARD),
    TANZANIA("TZ", CountryTier.STANDARD),
    UKRAINE("UA", CountryTier.STANDARD),
    UGANDA("UG", CountryTier.STANDARD),
    U_S_OUTLYING_ISLANDS("UM", CountryTier.STANDARD),
    UNITED_STATES("US", CountryTier.COMMON),
    URUGUAY("UY", CountryTier.STANDARD),
    UZBEKISTAN("UZ", CountryTier.STANDARD),
    VATICAN_CITY("VA", CountryTier.STANDARD),
    ST_VINCENT_GRENADINES("VC", CountryTier.STANDARD),
    VENEZUELA("VE", CountryTier.STANDARD),
    BRITISH_VIRGIN_ISLANDS("VG", CountryTier.STANDARD),
    U_S_VIRGIN_ISLANDS("VI", CountryTier.STANDARD),
    VIETNAM("VN", CountryTier.STANDARD),
    VANUATU("VU", CountryTier.STANDARD),
    WALLIS_FUTUNA("WF", CountryTier.STANDARD),
    SAMOA("WS", CountryTier.STANDARD),
    YEMEN("YE", CountryTier.STANDARD),
    MAYOTTE("YT", CountryTier.STANDARD),
    SOUTH_AFRICA("ZA", CountryTier.STANDARD),
    ZAMBIA("ZM", CountryTier.STANDARD),
    ZIMBABWE("ZW", CountryTier.STANDARD),
                ;

    private final String isoCode;
    private final CountryTier tier;

    Country(String isoCode, CountryTier tier) {
        this.isoCode = isoCode;
        this.tier = tier;
    }

    @JsonValue
    public String getIsoCode() {
        return isoCode;
    }

    public CountryTier getTier() {
        return tier;
    }

    public String getDisplayName(Locale locale) {
        return new Locale("", isoCode).getDisplayCountry(locale);
    }

    @JsonCreator
    public static Country fromJson(String value) {
        return parse(value);
    }

    public static Country fromIsoCode(String isoCode) {
        if (isoCode == null || isoCode.isBlank()) {
            return null;
        }
        String normalized = isoCode.trim().toUpperCase(Locale.ROOT);
        for (Country country : values()) {
            if (country.isoCode.equals(normalized)) {
                return country;
            }
        }
        throw new IllegalArgumentException("Unknown country ISO code: " + isoCode);
    }

    public static Country parse(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        try {
            return Country.valueOf(trimmed.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return fromIsoCode(trimmed);
        }
    }
}
