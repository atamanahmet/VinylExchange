package com.atamanahmet.vinylexchange.infrastructure;

import java.io.InputStream;

import lombok.Getter;

@Getter
public class ImageSource {

    private InputStream inputStream;
    private String originalFilename;
    private String contentType;
    private long size;

    public ImageSource(
            InputStream inputStream,
            String originalFilename,
            String contentType,
            long size) {

        this.inputStream = inputStream;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.size = size;
    }

}
