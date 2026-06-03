package com.atamanahmet.vinylexchange.domain.enums;

public enum CompressionType {
    LOSSLESS("Lossless"),
    LOSSY("Lossy");

    private String value;

    CompressionType(String value) {
        this.value = value;
    }

    // public String getValue() {
    // return value;
    // }

    @Override
    public String toString() {
        return this.value;
    }
}
