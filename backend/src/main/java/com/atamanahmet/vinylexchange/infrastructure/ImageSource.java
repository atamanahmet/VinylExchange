package com.atamanahmet.vinylexchange.infrastructure;

import lombok.Getter;

@Getter
public class ImageSource {

    private byte[] data;
    private String originalFilename;
    private String contentType;
    private long size;

    public ImageSource(
            byte[] data,
            String originalFilename,
            String contentType,
            long size) {

        this.data = data;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.size = size;
    }

}
