package com.atamanahmet.vinylexchange.exception;

public class PageNotFoundException extends RuntimeException {
    public PageNotFoundException() {
        super("Cms page not found");
    }
}
