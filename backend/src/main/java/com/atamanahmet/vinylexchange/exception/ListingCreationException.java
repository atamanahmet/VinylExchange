package com.atamanahmet.vinylexchange.exception;

public class ListingCreationException extends RuntimeException {
    public ListingCreationException(String message, String username) {
        super(message);
    }
}
