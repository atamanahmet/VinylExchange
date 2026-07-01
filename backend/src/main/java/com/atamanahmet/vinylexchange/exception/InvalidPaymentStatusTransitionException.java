package com.atamanahmet.vinylexchange.exception;

import com.atamanahmet.vinylexchange.domain.enums.PaymentStatus;

public class InvalidPaymentStatusTransitionException extends RuntimeException {

    public InvalidPaymentStatusTransitionException(PaymentStatus from, PaymentStatus to) {
        super("Cannot transition payment from " + from + " to " + to);
    }

    public InvalidPaymentStatusTransitionException(String message) {
        super(message);
    }
}
