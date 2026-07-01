package com.atamanahmet.vinylexchange.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.atamanahmet.vinylexchange.domain.embeddable.MediaInfo;
import com.atamanahmet.vinylexchange.domain.enums.MediaFormat;
import com.atamanahmet.vinylexchange.domain.enums.VinylSubtype;

class MusicBrainzFormatParserTest {

    @Test
    void fromMusicBrainzFormat_parsesVinylLp() {
        MediaInfo mediaInfo = MusicBrainzFormatParser.fromMusicBrainzFormat("12\" Vinyl");

        assertThat(mediaInfo.getFormat()).isEqualTo(MediaFormat.VINYL);
        assertThat(mediaInfo.getVinylSubtype()).isEqualTo(VinylSubtype.LP);
        assertThat(mediaInfo.getSpeedRpm()).isEqualTo(33);
        assertThat(mediaInfo.getVinylSize()).isEqualTo("12\"");
        assertThat(mediaInfo.getDiscCount()).isEqualTo(1);
        assertThat(mediaInfo.getSourceFormatRaw()).isEqualTo("12\" Vinyl");
    }

    @Test
    void fromMusicBrainzFormat_parsesDoubleLp() {
        MediaInfo mediaInfo = MusicBrainzFormatParser.fromMusicBrainzFormat("2×12\" Vinyl");

        assertThat(mediaInfo.getDiscCount()).isEqualTo(2);
    }

    @Test
    void fromMusicBrainzFormat_parsesCd() {
        MediaInfo mediaInfo = MusicBrainzFormatParser.fromMusicBrainzFormat("CD");

        assertThat(mediaInfo.getFormat()).isEqualTo(MediaFormat.CD);
        assertThat(mediaInfo.getDiscCount()).isEqualTo(1);
    }

    @Test
    void formatter_buildsReadableVinylLabel() {
        MediaInfo mediaInfo = MusicBrainzFormatParser.fromMusicBrainzFormat("2×12\" Vinyl");

        assertThat(MediaInfoFormatter.toDisplayLabel(mediaInfo)).isEqualTo("2xLP 12\" / 33");
    }
}
