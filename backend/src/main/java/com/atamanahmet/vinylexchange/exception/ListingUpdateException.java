package com.atamanahmet.vinylexchange.exception;

public class ListingUpdateException extends RuntimeException {
    public ListingUpdateException(String message, String listingId) {
        super(message + listingId);
    }
}