package com.atamanahmet.vinylexchange.infrastructure;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompressedImage {
    private String fileName;
    private byte[] image;
    // private byte[] losslessImage;
}
