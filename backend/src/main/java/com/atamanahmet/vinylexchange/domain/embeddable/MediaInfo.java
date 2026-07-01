package com.atamanahmet.vinylexchange.domain.embeddable;

import com.atamanahmet.vinylexchange.domain.enums.MediaFormat;
import com.atamanahmet.vinylexchange.domain.enums.VinylSubtype;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaInfo {

    @Enumerated(EnumType.STRING)
    private MediaFormat format;

    @Enumerated(EnumType.STRING)
    private VinylSubtype vinylSubtype;

    private Integer speedRpm;
    private String vinylSize;
    private Integer discCount;

    private Boolean colored;
    private Boolean pictureDisc;

    private String sourceFormatRaw;
}
