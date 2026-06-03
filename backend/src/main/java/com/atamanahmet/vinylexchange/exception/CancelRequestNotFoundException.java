package com.atamanahmet.vinylexchange.exception;

public class CancelRequestNotFoundException extends RuntimeException {
    public CancelRequestNotFoundException() {
        super("Cancel request not found");
    }

    public CancelRequestNotFoundException(String message) {
        super(message);
    }

}
