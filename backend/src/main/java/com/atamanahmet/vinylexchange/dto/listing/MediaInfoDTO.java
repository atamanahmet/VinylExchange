package com.atamanahmet.vinylexchange.dto.listing;

import com.atamanahmet.vinylexchange.domain.enums.MediaFormat;
import com.atamanahmet.vinylexchange.domain.enums.VinylSubtype;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaInfoDTO {

    @NotNull(message = "Media format is required")
    private MediaFormat format;

    private VinylSubtype vinylSubtype;
    private Integer speedRpm;
    private String vinylSize;
    private Integer discCount;
    private Boolean colored;
    private Boolean pictureDisc;
    private String sourceFormatRaw;
}
