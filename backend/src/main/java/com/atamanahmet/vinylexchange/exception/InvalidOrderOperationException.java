package com.atamanahmet.vinylexchange.exception;

public class InvalidOrderOperationException extends RuntimeException {
    public InvalidOrderOperationException() {
        super("Order not found");
    }

    public InvalidOrderOperationException(String message) {
        super(message);
    }
}
