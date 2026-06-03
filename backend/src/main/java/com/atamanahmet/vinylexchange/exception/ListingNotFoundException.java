package com.atamanahmet.vinylexchange.exception;

public class ListingNotFoundException extends RuntimeException {

    public ListingNotFoundException() {
        super("Listing is not exist or available anymore");
    }

    public ListingNotFoundException(String message) {
        super(message);
    }

}
