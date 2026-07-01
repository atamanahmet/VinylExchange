package com.atamanahmet.vinylexchange.mapper;

import com.atamanahmet.vinylexchange.domain.embeddable.MediaInfo;
import com.atamanahmet.vinylexchange.domain.enums.MediaFormat;
import com.atamanahmet.vinylexchange.domain.enums.VinylSubtype;

public final class MediaInfoFormatter {

    private MediaInfoFormatter() {
    }

    public static String toDisplayLabel(MediaInfo mediaInfo) {
        if (mediaInfo == null || mediaInfo.getFormat() == null) {
            return null;
        }

        return switch (mediaInfo.getFormat()) {
            case VINYL -> formatVinyl(mediaInfo);
            case CD -> formatDiscCountPrefix(mediaInfo.getDiscCount()) + "CD";
            case CASSETTE -> "Cassette";
            case EIGHT_TRACK -> "8-Track";
            case OTHER -> mediaInfo.getSourceFormatRaw() != null && !mediaInfo.getSourceFormatRaw().isBlank()
                    ? mediaInfo.getSourceFormatRaw()
                    : "Other";
        };
    }

    private static String formatVinyl(MediaInfo mediaInfo) {
        StringBuilder label = new StringBuilder(formatDiscCountPrefix(mediaInfo.getDiscCount()));

        VinylSubtype subtype = mediaInfo.getVinylSubtype();
        if (subtype != null) {
            label.append(switch (subtype) {
                case LP -> "LP";
                case EP -> "EP";
                case SINGLE -> "Single";
                case MAXI_SINGLE -> "Maxi Single";
            });
        } else {
            label.append("Vinyl");
        }

        if (mediaInfo.getVinylSize() != null && !mediaInfo.getVinylSize().isBlank()) {
            label.append(' ').append(mediaInfo.getVinylSize());
        }

        if (mediaInfo.getSpeedRpm() != null) {
            label.append(" / ").append(mediaInfo.getSpeedRpm());
        }

        if (Boolean.TRUE.equals(mediaInfo.getColored())) {
            label.append(" (Colored)");
        }

        if (Boolean.TRUE.equals(mediaInfo.getPictureDisc())) {
            label.append(" (Picture Disc)");
        }

        return label.toString().trim();
    }

    private static String formatDiscCountPrefix(Integer discCount) {
        if (discCount != null && discCount > 1) {
            return discCount + "x";
        }
        return "";
    }
}
