package com.atamanahmet.vinylexchange.exception;

public class OrderItemNotFoundException extends RuntimeException {
    public OrderItemNotFoundException() {
        super("OrderItem not found");
    }

    public OrderItemNotFoundException(String message) {
        super(message);
    }
}
