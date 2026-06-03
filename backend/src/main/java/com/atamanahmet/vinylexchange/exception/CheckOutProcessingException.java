package com.atamanahmet.vinylexchange.exception;

public class CheckOutProcessingException extends RuntimeException {
    public CheckOutProcessingException() {
        super("Checkout processing failed");
    }
}
