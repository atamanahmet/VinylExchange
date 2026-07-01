package com.atamanahmet.vinylexchange.mapper;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.atamanahmet.vinylexchange.domain.embeddable.MediaInfo;
import com.atamanahmet.vinylexchange.domain.enums.MediaFormat;
import com.atamanahmet.vinylexchange.domain.enums.VinylSubtype;

public final class MusicBrainzFormatParser {

    private static final Pattern DISC_COUNT = Pattern.compile("(\\d+)\\s*[x×]", Pattern.CASE_INSENSITIVE);
    private static final Pattern SIZE = Pattern.compile("(\\d+)\\s*\"");
    private static final Pattern RPM = Pattern.compile("(33|45|78)\\s*(?:rpm)?", Pattern.CASE_INSENSITIVE);

    private MusicBrainzFormatParser() {
    }

    public static MediaInfo fromMusicBrainzFormat(String raw) {
        if (raw == null || raw.isBlank()) {
            return MediaInfo.builder()
                    .format(MediaFormat.OTHER)
                    .build();
        }

        String normalized = raw.trim();
        String upper = normalized.toUpperCase(Locale.ROOT);

        if (upper.contains("CD")) {
            return MediaInfo.builder()
                    .format(MediaFormat.CD)
                    .discCount(parseDiscCount(normalized, 1))
                    .sourceFormatRaw(normalized)
                    .build();
        }

        if (upper.contains("CASSETTE")) {
            return MediaInfo.builder()
                    .format(MediaFormat.CASSETTE)
                    .sourceFormatRaw(normalized)
                    .build();
        }

        if (upper.contains("8-TRACK") || upper.contains("8 TRACK") || upper.contains("8TRACK")) {
            return MediaInfo.builder()
                    .format(MediaFormat.EIGHT_TRACK)
                    .sourceFormatRaw(normalized)
                    .build();
        }

        if (upper.contains("VINYL") || upper.contains("LP") || upper.contains("EP")
                || SIZE.matcher(normalized).find()) {
            return parseVinyl(normalized);
        }

        return MediaInfo.builder()
                .format(MediaFormat.OTHER)
                .sourceFormatRaw(normalized)
                .build();
    }

    private static MediaInfo parseVinyl(String raw) {
        String upper = raw.toUpperCase(Locale.ROOT);
        VinylSubtype subtype = VinylSubtype.LP;

        if (upper.contains("MAXI")) {
            subtype = VinylSubtype.MAXI_SINGLE;
        } else if (upper.contains(" EP") || upper.startsWith("EP") || upper.contains("EP ")) {
            subtype = VinylSubtype.EP;
        } else if (upper.contains("SINGLE") || upper.contains("7\"")) {
            subtype = VinylSubtype.SINGLE;
        }

        Integer speed = parseRpm(raw);
        if (speed == null && subtype == VinylSubtype.SINGLE) {
            speed = 45;
        }
        if (speed == null && (subtype == VinylSubtype.LP || subtype == VinylSubtype.EP)) {
            speed = 33;
        }

        String size = parseSize(raw);
        if (size == null && subtype == VinylSubtype.SINGLE) {
            size = "7\"";
        }
        if (size == null && (subtype == VinylSubtype.LP || subtype == VinylSubtype.EP)) {
            size = "12\"";
        }

        boolean colored = upper.contains("COLOU") || upper.contains("COLOR");
        boolean pictureDisc = upper.contains("PICTURE DISC");

        return MediaInfo.builder()
                .format(MediaFormat.VINYL)
                .vinylSubtype(subtype)
                .speedRpm(speed)
                .vinylSize(size)
                .discCount(parseDiscCount(raw, 1))
                .colored(colored ? true : null)
                .pictureDisc(pictureDisc ? true : null)
                .sourceFormatRaw(raw)
                .build();
    }

    private static Integer parseDiscCount(String raw, int defaultValue) {
        Matcher matcher = DISC_COUNT.matcher(raw);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return defaultValue;
    }

    private static Integer parseRpm(String raw) {
        Matcher matcher = RPM.matcher(raw);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return null;
    }

    private static String parseSize(String raw) {
        Matcher matcher = SIZE.matcher(raw);
        if (matcher.find()) {
            return matcher.group(1) + "\"";
        }
        return null;
    }
}
