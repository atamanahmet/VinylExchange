package com.atamanahmet.vinylexchange.exception;

import java.util.List;

import com.atamanahmet.vinylexchange.dto.order.CartValidationIssue;

public class CheckOutValidationException extends RuntimeException {
    public CheckOutValidationException(List<CartValidationIssue> issues) {
        super("Cart has one or more validation errors, cannot proceed: " + issues.toString());
    }
}
