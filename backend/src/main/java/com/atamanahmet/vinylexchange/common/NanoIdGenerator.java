package com.atamanahmet.vinylexchange.common;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;

/**
 * Generates url-safe nano ids for public-facing identifiers.
 */
public final class NanoIdGenerator {

    private static final char[] ALPHABET =
            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();
    private static final int DEFAULT_SIZE = 12;

    private NanoIdGenerator() {}

    public static String generate() {
        return NanoIdUtils.randomNanoId(
                NanoIdUtils.DEFAULT_NUMBER_GENERATOR,
                ALPHABET,
                DEFAULT_SIZE);
    }
}
