package com.atamanahmet.vinylexchange.exception;

public class EmptyCartException extends RuntimeException {
    public EmptyCartException() {
        super("Cart is Empty");
    }

}
