package com.atamanahmet.vinylexchange.exception;

public class GenreNotFoundException extends RuntimeException {
    public GenreNotFoundException() {
        super("Genre Not Found");
    }
}
